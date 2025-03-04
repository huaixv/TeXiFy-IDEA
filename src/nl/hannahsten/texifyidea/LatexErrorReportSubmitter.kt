package nl.hannahsten.texifyidea

import arrow.resilience.Schedule
import arrow.resilience.retry
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.intellij.ide.BrowserUtil
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.Consumer
import kotlinx.coroutines.runBlocking
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import java.awt.Component
import java.io.UnsupportedEncodingException
import java.net.*
import kotlin.time.Duration.Companion.milliseconds

/**
 * Send error report to GitHub issue tracker.
 *
 * @author Sten Wessel
 */
class LatexErrorReportSubmitter : ErrorReportSubmitter() {

    override fun getReportActionText() = "Report to TeXiFy-IDEA Issue Tracker"

    override fun submit(
        events: Array<out IdeaLoggingEvent>,
        additionalInfo: String?,
        parentComponent: Component,
        consumer: Consumer<in SubmittedReportInfo>
    ): Boolean {
        val retrySchedule = Schedule.exponential<Throwable>(250.milliseconds)

        val latestVersion = try {
            runBlocking {
                retrySchedule.retry { Util.getLatestVersion() }
            }
        }
        // Don't do the check when there's no internet connection
        catch (e: UnknownHostException) {
            return true
        }

        if (latestVersion.version.toString().isNotBlank() && DefaultArtifactVersion(Util.currentVersion) < latestVersion.version) {
            val currentIdeaVersion = DefaultArtifactVersion(ApplicationInfo.getInstance().build.asStringWithoutProductCode())
            val requiredIdeaVersion = latestVersion.ideaVersion.sinceBuild

            JBPopupFactory.getInstance().createMessage("")
                .showInCenterOf(parentComponent)

            val message = "Please update your current version (${Util.currentVersion}) of TeXiFy to the latest version (${latestVersion.version}) before submitting,\n" +
                "to check if the error is already fixed. Go to Settings > Plugins to update.\n" +
                if (currentIdeaVersion < requiredIdeaVersion) "You first need to update your current IDE version ($currentIdeaVersion) to $requiredIdeaVersion or newer.\n" else ""

            val result = MessageDialogBuilder.okCancel("Update TeXiFy", message)
                .yesText("Cancel Submit") // Sort of the wrong way around, but it suggests to cancel this way
                .noText("Submit Anyway")
                .ask(parentComponent)

            // If the user cancels, don't submit
            return !result
        }

        submit(events, additionalInfo, consumer)
        return true
    }

    private fun submit(events: Array<out IdeaLoggingEvent>?, additionalInfo: String?, consumer: Consumer<in SubmittedReportInfo>): Boolean {
        val event = events?.firstOrNull()
        val title = event?.throwableText?.lineSequence()?.first()
            ?: event?.message
            ?: "Crash Report: <Fill in title>"
        val body = event?.throwableText ?: "Please paste the full stacktrace from the IDEA error popup."
        // In some cases, very long stacktraces can refer to TeXiFy code only at the bottom (after 7000 characters), but we do want to submit this information, so we trim the middle part of the stacktrace
        val maximumUrlSize = 6000
        val smallBody = if ("at nl.hannahsten.texifyidea" in body && "at nl.hannahsten.texifyidea" !in body.take(maximumUrlSize)) {
            Util.filterInterestingLines(body)
        }
        else {
            body
        }

        val builder = StringBuilder(ISSUE_URL)
        try {
            builder.append(URLEncoder.encode(title.take(500), ENCODING))
            builder.append("&body=")

            val applicationInfo = ApplicationInfo.getInstance().let { "${it.fullApplicationName} (build ${it.build})" }
            builder.append(URLEncoder.encode("### Type of JetBrains IDE (IntelliJ, PyCharm, etc.) and version\n${applicationInfo}\n\n", ENCODING))
            val systemInfo = "${SystemInfo.OS_NAME} ${SystemInfo.OS_VERSION} (${SystemInfo.OS_ARCH})"
            builder.append(URLEncoder.encode("### Operating System \n$systemInfo\n\n", ENCODING))
            builder.append(URLEncoder.encode("### TeXiFy IDEA version\n${Util.currentVersion}\n\n", ENCODING))
            builder.append(URLEncoder.encode("### Description\n", ENCODING))
            builder.append(URLEncoder.encode(additionalInfo ?: "\n", ENCODING))
            builder.append(URLEncoder.encode("\n\n### Stacktrace\n```\n${smallBody.take(6000)}\n```", ENCODING))
        }
        catch (e: UnsupportedEncodingException) {
            consumer.consume(
                SubmittedReportInfo(
                    null,
                    null,
                    SubmittedReportInfo.SubmissionStatus.FAILED
                )
            )
            return false
        }

        BrowserUtil.browse(builder.toString())
        consumer.consume(
            SubmittedReportInfo(
                null,
                "GitHub issue",
                SubmittedReportInfo.SubmissionStatus.NEW_ISSUE
            )
        )
        return true
    }

    object Util {
        private var latestVersionCached = IdeaPlugin()
        fun getLatestVersion(): IdeaPlugin {
            if (latestVersionCached.version.toString().isNotBlank()) return latestVersionCached

            // Create xml mapper that doesn't fail on unknown properties. This allows us to only define the properties we need.
            val mapper = XmlMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

            val connection = (URI.create(JETBRAINS_API_URL).toURL().openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 1000
                readTimeout = 1000
            }

            val inputString: String = try {
                connection.inputStream.reader().use { it.readText() }
            }
            catch (e: SocketTimeoutException) {
                return latestVersionCached
            }
            latestVersionCached = mapper.readValue(inputString, PluginRepo::class.java)
                .category
                ?.maxByOrNull { it.version }
                ?: latestVersionCached

            return latestVersionCached
        }

        val currentVersion by lazy {
            PluginManagerCore.getPlugin(PluginId.getId("nl.rubensten.texifyidea"))?.version
        }

        /**
         * If the stacktrace is too long, collect lines referring to TeXiFy only.
         */
        fun filterInterestingLines(body: String): String {
            val lines = body.split("\n").filter { it.isNotBlank() }
            val texifyLines = lines.mapIndexedNotNull { i: Int, line: String -> if ("nl.hannahsten.texifyidea" in line || "Caused by:" in line) i else null }
            val interestingLines = ((0..10).toSet() + texifyLines.flatMap { listOf(it - 1, it, it + 1) }.toSet()).toList().sorted()
            return interestingLines.foldIndexed("") { i, stacktrace, lineIndex ->
                stacktrace + (if (i > 0 && interestingLines[i - 1] < lineIndex - 1) "\n        (...)" else "") + "\n" + lines.getOrElse(lineIndex) { "" }.take(500)
            }.trim()
        }
    }

    companion object {

        private const val ISSUE_URL = "https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/new?labels=crash-report&template=crash_report.md&title="

        private const val JETBRAINS_API_URL = "https://plugins.jetbrains.com/plugins/list?pluginId=9473"

        private const val ENCODING = "UTF-8"
    }

    /**
     * Plugin repo data class which is the root of the xml response from the [Marketplace API](https://plugins.jetbrains.com/docs/marketplace/api-reference.html).
     */
    data class PluginRepo(
        /**
         * Wrapper for multiple idea-plugin objects. Has to be nullable AND have default value to avoid exception.
         */
        @JacksonXmlProperty(localName = "idea-plugin")
        @JacksonXmlElementWrapper(useWrapping = false)
        val category: List<IdeaPlugin>? = emptyList()
    )

    /**
     * Data class specifying all the properties we need from the idea-plugin tag from the xml response.
     * All properties have to have a default value, because the class needs to have an empty constructor.
     */
    data class IdeaPlugin(
        @JacksonXmlProperty(localName = "version")
        val version: DefaultArtifactVersion = DefaultArtifactVersion(""),
        @JacksonXmlProperty(localName = "idea-version")
        val ideaVersion: IdeaVersion = IdeaVersion()
    )

    data class IdeaVersion(
        @JacksonXmlProperty(isAttribute = true, localName = "since-build")
        val sinceBuild: DefaultArtifactVersion = DefaultArtifactVersion("")
    )
}
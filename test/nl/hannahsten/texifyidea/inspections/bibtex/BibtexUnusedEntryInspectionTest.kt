package nl.hannahsten.texifyidea.inspections.bibtex

import nl.hannahsten.texifyidea.configureByFilesWithMockCache
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.testutils.writeCommand

class BibtexUnusedEntryInspectionTest : TexifyInspectionTestBase(BibtexUnusedEntryInspection()) {

    override fun getTestDataPath(): String {
        return "test/resources/inspections/bibtex/unusedbibentry"
    }

    fun `test warnings where needed`() {
        myFixture.configureByFilesWithMockCache("references.bib", "main.tex")
        myFixture.checkHighlighting()
    }

    fun `test quick fix`() {
        myFixture.configureByFilesWithMockCache("references-before.bib", "main-quick-fix.tex")
        val quickFixes = myFixture.getAllQuickFixes()
        assertEquals("Expected number of quick fixes:", 2, quickFixes.size)
        writeCommand(myFixture.project) {
            quickFixes.firstOrNull()?.invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResultByFile("references-after.bib")
    }
}
package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.LISTINGS
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.TCOLORBOX
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.XARGS

/**
 * @author Hannah Schellekens
 */
enum class LatexNewDefinitionCommand(
    override val command: String,
    override vararg val arguments: Argument = emptyArray(),
    override val dependency: LatexPackage = LatexPackage.DEFAULT,
    override val display: String? = null,
    override val isMathMode: Boolean = false,
    val collapse: Boolean = false
) : LatexCommand {

    CATCODE("catcode"),
    NEWCOMMAND("newcommand", "cmd".asRequired(), "args".asOptional(), "default".asOptional(), "def".asRequired(Argument.Type.TEXT)),
    NEWCOMMAND_STAR("newcommand*", "cmd".asRequired(), "args".asOptional(), "default".asOptional(), "def".asRequired(Argument.Type.TEXT)),
    NEWIF("newif", "cmd".asRequired()),
    PROVIDECOMMAND("providecommand", "cmd".asRequired(), "args".asOptional(), "default".asOptional(), "def".asRequired(Argument.Type.TEXT)),
    PROVIDECOMMAND_STAR("providecommand*", "cmd".asRequired(), "args".asOptional(), "default".asOptional(), "def".asRequired(Argument.Type.TEXT)),
    RENEWCOMMAND("renewcommand", "cmd".asRequired(), "args".asOptional(), "default".asOptional(), "def".asRequired(Argument.Type.TEXT)),
    RENEWCOMMAND_STAR("renewcommand*", "cmd".asRequired(), "args".asOptional(), "default".asOptional(), "def".asRequired(Argument.Type.TEXT)),
    NEWENVIRONMENT("newenvironment", "name".asRequired(), "args".asOptional(), "default".asOptional(), "begdef".asRequired(Argument.Type.TEXT), "enddef".asRequired(Argument.Type.TEXT)),
    RENEWENVIRONMENT("renewenvironment", "name".asRequired(), "args".asOptional(), "default".asOptional(), "begdef".asRequired(Argument.Type.TEXT), "enddef".asRequired(Argument.Type.TEXT)),
    NEWTCOLORBOX("newtcolorbox", "init options".asOptional(), "name".asRequired(), "number".asOptional(), "default".asOptional(), "options".asRequired(), dependency = TCOLORBOX),
    RENEWTCOLORBOX("newtcolorbox", "init options".asOptional(), "name".asRequired(), "number".asOptional(), "default".asOptional(), "options".asRequired(), dependency = TCOLORBOX),
    DECLARETCOLORBOX("DeclareTColorBox", "init options".asOptional(), "name".asRequired(), "specification".asRequired(), "options".asRequired(), dependency = TCOLORBOX),
    NEWTCOLORBOX_("NewTColorBox", "init options".asOptional(), "name".asRequired(), "specification".asRequired(), "options".asRequired(), dependency = TCOLORBOX),
    RENEWTCOLORBOX_("ReNewTColorBox", "init options".asOptional(), "name".asRequired(), "specification".asRequired(), "options".asRequired(), dependency = TCOLORBOX),
    PROVIDETCOLORBOX("ProvideTColorBox", "init options".asOptional(), "name".asRequired(), "specification".asRequired(), "options".asRequired(), dependency = TCOLORBOX),
    NEWCOMMANDX("newcommandx", "cmd".asRequired(), "args".asOptional(), "default".asOptional(), "def".asRequired(Argument.Type.TEXT), dependency = XARGS),
    RENEWCOMMANDX("renewcommandx", "cmd".asRequired(), "args".asOptional(), "default".asOptional(), "def".asRequired(Argument.Type.TEXT), dependency = XARGS),
    PROVIDECOMMANDX("providecommandx", "cmd".asRequired(), "args".asOptional(), "default".asOptional(), "def".asRequired(Argument.Type.TEXT), dependency = XARGS),
    DECLAREROBUSTCOMMANDX("DeclareRobustCommandx", "cmd".asRequired(), "args".asOptional(), "default".asOptional(), "def".asRequired(Argument.Type.TEXT), dependency = XARGS),
    NEWENVIRONMENTX("newenvironmentx", "cmd".asRequired(), "args".asOptional(), "default".asOptional(), "begdef".asRequired(Argument.Type.TEXT), "enddef".asRequired(Argument.Type.TEXT), dependency = XARGS),
    RENEWENVIRONMENTX("renewenvironmentx", "cmd".asRequired(), "args".asOptional(), "default".asOptional(), "begdef".asRequired(Argument.Type.TEXT), "enddef".asRequired(Argument.Type.TEXT), dependency = XARGS),
    LSTNEWENVIRONMENT("lstnewenvironment", "name".asRequired(), "number".asOptional(), "default arg".asOptional(), "starting code".asRequired(), "ending code".asRequired(), dependency = LISTINGS),
    ;

    override val identifier: String
        get() = name
}
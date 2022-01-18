package xiaoLanguage.util

import xiaoLanguage.tokens.Position

sealed class Report {
    abstract val exception: Exception
    abstract val code: Code?
    abstract val help: List<Help>?

    data class Error(
        override val exception: Exception,
        override val code: Code? = null,
        override val help: List<Help>? = null
    ) : Report()

    data class Warning(
        override val exception: Exception,
        override val code: Code? = null,
        override val help: List<Help>? = null
    ) : Report()

    data class Debug(
        override val exception: Exception,
        override val code: Code? = null,
        override val help: List<Help>? = null
    ) : Report()

    data class Help(
        val code: String,
        val helpHintString: String
    ) {
        fun output(): String {
            return code.split("\n").mapIndexed { index, s ->
                s.replaceIndentByMargin("$index| ".padStart(9))
            }.joinToString("\n")
        }
    }

    data class Code(
        val startLine: Int,
        val arrowStart: Position,
        val arrowEnd: Position = arrowStart,
        val endLine: Int = arrowEnd.lineNumber,
    ) {
        fun output(source: List<String>): String {
            var outputText = ""
            for (lineNumber in startLine..endLine) {
                val code = source[lineNumber]
                val indentSpace = code.length - source[lineNumber].trimIndent().length
                outputText += "${lineNumber + 1}".padStart(7) + "| $code\n"
                if (lineNumber >= arrowStart.lineNumber && lineNumber <= arrowEnd.lineNumber) {
                    outputText += (0..6).joinToString("") { " " } + "| "
                    code.mapIndexed { index, _ ->
                        outputText += if (
                            arrowEnd.lineNumber == arrowStart.lineNumber &&
                            index <= arrowEnd.end &&
                            index >= arrowStart.start
                        ) "^"
                        else if (arrowEnd.lineNumber != arrowStart.lineNumber &&
                            ((lineNumber == startLine && arrowStart.start <= index) ||
                                    (lineNumber == endLine && arrowEnd.end >= index && index >= indentSpace))
                        ) "^"
                        else if (lineNumber in (startLine + 1) until endLine && index >= indentSpace) "^"
                        else " "
                    }
                    outputText += if (lineNumber != endLine) "\n" else ""
                }

            }

            return outputText
        }
    }

    enum class Color(val asciiColor: String) {
        Error("\u001b[31m"),
        End("\u001b[0m"),
        Warning("\u001b[33m"),
        Debug("\u001b[34m")
    }

    fun printReport(source: List<String>, path: String) {
        val exceptionName = exception::class.java.simpleName
        val level = this::class.java.simpleName

        val outputText = if (code != null) {
            """
            |${Color.valueOf(level).asciiColor}$exceptionName: ${exception.message} ${Color.End.asciiColor}
            |   $path:${code!!.arrowStart.lineNumber + 1}:${code!!.arrowStart.start}
            |
            """.trimMargin() + code!!.output(source) + (
                    help?.joinToString("\n") {
                        "\n   help: ${it.helpHintString}" +
                                it.output()
                    } ?: ""
                    )
        } else {
            "${Color.valueOf(level).asciiColor}$exceptionName: ${exception.message} ${Color.End.asciiColor}"
        }

        println(outputText)
    }
}

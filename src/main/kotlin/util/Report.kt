package xiaoLanguage.util

import xiaoLanguage.tokens.Position

sealed class Report {
    abstract val exception: Exception
    abstract val start: Position?
    abstract val end: Position?
    abstract val help: List<Help>?

    data class Error(
        override val exception: Exception,
        override val start: Position? = null,
        override val end: Position? = start,
        override val help: List<Help>? = null
    ) : Report()

    data class Warning(
        override val exception: Exception,
        override val start: Position? = null,
        override val end: Position? = start,
        override val help: List<Help>? = null
    ) : Report()

    data class Debug(
        override val exception: Exception,
        override val start: Position? = null,
        override val end: Position? = start,
        override val help: List<Help>? = null
    ) : Report()

    data class Help(
        val code: String,
        val helpHintString: String
    ) {
        fun output(): String {
            return code.split("\n").mapIndexed { index, s ->
                s.replaceIndentByMargin("  $index |")
            }.joinToString("\n")
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

        val outputText = if (start != null) {
            val code = source[start!!.lineNumber].trimIndent()
            val arrow = source[start!!.lineNumber].length - code.length
            val arrowNumber = if (end == null) 0 else {
                if (start != null) end!!.end - start!!.start
                else 0
            }

            """
            |${Color.valueOf(level).asciiColor}$exceptionName: ${exception.message} ${Color.End.asciiColor}
            |   "$path":${start!!.lineNumber + 1}:${start!!.start}
            |      > $code
            |      ${(0..start!!.start + 1 - arrow).joinToString("") { " " }}${(0..arrowNumber).joinToString("") { "^" }}
            """.trimMargin() + (
                    help?.joinToString("\n") {
                        "\nhelp: ${it.helpHintString}" +
                                it.output()
                    } ?: ""
                    )
        } else {
            "${Color.valueOf(level).asciiColor}$exceptionName: ${exception.message} ${Color.End.asciiColor}"
        }

        println(outputText)
    }
}

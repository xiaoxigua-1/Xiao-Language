package xiaoLanguage.util

import xiaoLanguage.tokens.Position

sealed class Report {
    abstract val exception: Exception
    abstract val start: Position?
    abstract val end: Position?

    data class Error(
        override val exception: Exception,
        override val start: Position? = null,
        override val end: Position? = null
    ) : Report()

    data class Warning(
        override val exception: Exception,
        override val start: Position? = null,
        override val end: Position? = null
    ) : Report()

    data class Debug(
        override val exception: Exception,
        override val start: Position? = null,
        override val end: Position? = null
    ) : Report()

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
            |File "$path", ${start!!.lineNumber + 1} line
            |  > $code
            |  ${(0..start!!.start + 1 - arrow).joinToString("") { " " }}${(0..arrowNumber).joinToString("") { "^" }}
            |${Color.valueOf(level).asciiColor}$exceptionName: ${exception.message} ${Color.End.asciiColor}
            |
            """.trimMargin()
        } else {
            "${Color.valueOf(level).asciiColor}$exceptionName: ${exception.message} ${Color.End.asciiColor}"
        }

        println(outputText)
    }
}

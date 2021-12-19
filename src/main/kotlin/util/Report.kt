package xiaoLanguage.util

import xiaoLanguage.tokens.Position

sealed class Report {
    abstract val exception: Exception
    abstract val position: Position?

    data class Error(override val exception: Exception, override val position: Position? = null) : Report()
    data class Warning(override val exception: Exception, override val position: Position? = null) : Report()
    data class Debug(override val exception: Exception, override val position: Position? = null) : Report()

    enum class Color(val asciiColor: String) {
        Error("\u001b[31m"),
        End("\u001b[0m"),
        Warning("\u001b[33m"),
        Debug("\u001b[34m")
    }

    fun printReport(source: List<String>, path: String) {
        val exceptionName = exception::class.java.simpleName
        val level = this::class.java.simpleName
        var outputText = ""

        outputText = if (position != null) {
            val code = source[position!!.lineNumber].trimIndent()
            val arrow = source[position!!.lineNumber].length - code.length

            """
            |File "$path", ${position!!.lineNumber + 1}
            |  > $code
            |  ${(0..position!!.start + 1 - arrow).joinToString("") { " " }}^
            |${Color.valueOf(level).asciiColor}$exceptionName: ${exception.message} ${Color.End.asciiColor}
            |
            """.trimMargin()
        } else {
            "${Color.valueOf(level).asciiColor}$exceptionName: ${exception.message} ${Color.End.asciiColor}"
        }

        println(outputText)
    }
}

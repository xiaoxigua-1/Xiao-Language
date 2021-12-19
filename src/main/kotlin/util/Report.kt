package xiaoLanguage.util

import xiaoLanguage.tokens.Position

sealed class Report {
    abstract val exception: Exception
    abstract val position: Position?

    data class Error(override val exception: Exception, override val position: Position?) : Report()
    data class Warning(override val exception: Exception, override val position: Position?) : Report()
    data class Debug(override val exception: Exception, override val position: Position?) : Report()

    fun printReport(source: List<String>, path: String) {
        val exceptionName = exception::class.java.name

    }
}

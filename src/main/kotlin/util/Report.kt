package xiaoLanguage.util

import xiaoLanguage.tokens.Position

class Report(private val exception: Exception, val position: Position?) {
    fun printReport(source: List<String>, path: String) {
        val exceptionName = exception::class.java.name

    }
}

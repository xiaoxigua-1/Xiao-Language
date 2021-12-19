package xiaoLanguage.parser

import xiaoLanguage.lexer.Lexer
import xiaoLanguage.tokens.Position
import xiaoLanguage.tokens.Token
import xiaoLanguage.util.Report.*
import xiaoLanguage.util.Report
import java.io.File

class Parser(private val lex: Lexer, private val file: File) {
    private val parserReporter = mutableListOf<Report>()
    private var tokens: MutableList<Token> = mutableListOf()
    private var lexerReport = mutableListOf<Report>()

    init {
        try {
            tokens = lex.lex()
        } catch (e: Exception) {
            lexerReport.add(Error(e, Position(lex.lineNumber, lex.exceptionIndex)))
        }
    }

    fun parser() {
        if (lexerReport.filterIsInstance<Error>().isEmpty()) expression()
        else {
            lexerReport.forEach {
                it.printReport(file.readLines(), file.absolutePath)
            }
        }
    }

    private fun expression() {

    }
}
package xiaoLanguage.parser

import xiaoLanguage.lexer.Lexer
import xiaoLanguage.tokens.Position
import xiaoLanguage.tokens.Token
import xiaoLanguage.util.Report

class Parser(private val lex: Lexer, private val path: String) {
    private val parserReporter = mutableListOf<Report>()
    private var tokens: MutableList<Token> = mutableListOf()
    private var lexerReport = mutableListOf<Report>()

    init {
        try {
            tokens = lex.lex()
        } catch (e: Exception) {
            lexerReport.add(Report(e, Position(lex.lineNumber, lex.exceptionIndex)))
        }
    }

    fun parser() {

    }
}
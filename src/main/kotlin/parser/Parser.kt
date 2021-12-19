package xiaoLanguage.parser

import xiaoLanguage.lexer.Lexer
import xiaoLanguage.tokens.Position
import xiaoLanguage.tokens.Token
import xiaoLanguage.util.Report

class Parser(private val lex: Lexer, private val path: String) {
    private val reporter = mutableListOf<Report>()
    private var tokens: MutableList<Token> = mutableListOf()

    init {
        try {
            tokens = lex.lex()
        } catch (e: Exception) {
            reporter.add(Report(e, Position(lex.lineNumber, lex.exceptionIndex)))
        }
    }

    fun parser() {

    }
}
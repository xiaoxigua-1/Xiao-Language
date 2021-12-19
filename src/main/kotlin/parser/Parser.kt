package xiaoLanguage.parser

import xiaoLanguage.lexer.Lexer
import xiaoLanguage.tokens.Position
import xiaoLanguage.tokens.Token
import xiaoLanguage.tokens.TokenType
import xiaoLanguage.util.Report.*
import xiaoLanguage.util.Report
import java.io.File

class Parser(private val lex: Lexer, private val file: File) {
    private val parserReporter = mutableListOf<Report>()
    private var tokens: MutableList<Token> = mutableListOf()
    private val lexerReport = mutableListOf<Report>()
    private var index = 0

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

    private fun nextToken() {
        index++
    }

    private fun backToken() {
        index--
    }

    private fun comparison(token: TokenType): Token? = when {
        tokens[index].tokenType == token -> tokens[index]
        tokens.isEmpty() -> {
//            parserReporter.add(Report.Error(""))
            null
        }
        else -> null
    }

    private fun expression() {

    }
}
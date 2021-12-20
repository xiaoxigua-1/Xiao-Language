package xiaoLanguage.parser

import xiaoLanguage.ast.Function
import xiaoLanguage.ast.Parameter
import xiaoLanguage.ast.Statement
import xiaoLanguage.ast.Type
import xiaoLanguage.exception.SyntaxError
import xiaoLanguage.lexer.Lexer
import xiaoLanguage.tokens.Keyword
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
    private var ast = mutableListOf<Any>()
    private var index = 0

    init {
        try {
            tokens = lex.lex()
        } catch (e: Exception) {
            lexerReport.add(Error(e, Position(lex.lineNumber, lex.exceptionIndex)))
        }
    }

    fun parser() {
        if (lexerReport.filterIsInstance<Error>().isEmpty()) {
            try {
                expression()
            } catch (e: Exception) {
                parserReporter.forEach {
                    it.printReport(file.readLines(), file.absolutePath)
                }
            }
        } else {
            lexerReport.forEach {
                it.printReport(file.readLines(), file.absolutePath)
            }
        }
    }

    private val isEOFToken: Boolean
        get() = index > tokens.size

    private fun comparison(token: TokenType): Token = when {
        tokens[++index].tokenType == token -> tokens[index]
        tokens.isEmpty() -> {
            throw Exception()
        }
        else -> {
            parserReporter.add(Report.Error(SyntaxError("Unexpected token ${tokens[++index].tokenType}, expected token $token")))
            throw Exception()
        }
    }

    private fun expression() {
        while (!isEOFToken) {
            val node = when (tokens[index].literal) {
                Keyword.CLASS_KEYWORD.keyword -> classExpression()
                else -> null
            }

            if (node != null) ast += node
            else break
        }

        println(ast)
    }

    private fun classExpression(): xiaoLanguage.ast.Class {
        val classKeyword = tokens[index]
        val className = comparison(TokenType.IDENTIFIER_TOKEN)
        val functions = mutableListOf<Function>()

        comparison(TokenType.LEFT_CURLY_BRACKETS_TOKEN)
        // TODO(parse functions)
        comparison(TokenType.RIGHT_CURLY_BRACKETS_TOKEN)

        return xiaoLanguage.ast.Class(classKeyword = classKeyword, className = className, functions = functions)
    }

    private fun functionExpression(): Function {
        val fnKeyword = tokens[index]
        val fnName = comparison(TokenType.IDENTIFIER_TOKEN)
        val parameters = mutableListOf<Parameter>()
        val statements = mutableListOf<Statement>()

        comparison(TokenType.LEFT_SQUARE_BRACKETS_TOKEN)
        // TODO(parse )
        comparison(TokenType.RIGHT_SQUARE_BRACKETS_TOKEN)

        val colon = comparison(TokenType.COLON_TOKEN)
        val returnType = comparison(TokenType.IDENTIFIER_TOKEN)

        comparison(TokenType.LEFT_CURLY_BRACKETS_TOKEN)
        // TODO()
        comparison(TokenType.RIGHT_CURLY_BRACKETS_TOKEN)
        return Function(
            functionKeyword = fnKeyword,
            functionName = fnName,
            parameters = parameters,
            colon = colon,
            returnType = Type(returnType),
            statements = statements
        )
    }

    fun parameterExpression(): Parameter {
        val name = tokens[index]
        val colon = comparison(TokenType.COLON_TOKEN)
        val type = comparison(TokenType.IDENTIFIER_TOKEN)

        return Parameter(name, colon, Type(type))
    }
}
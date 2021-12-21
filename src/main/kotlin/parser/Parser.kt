package xiaoLanguage.parser

import xiaoLanguage.ast.*
import xiaoLanguage.ast.Function
import xiaoLanguage.exception.SyntaxError
import xiaoLanguage.lexer.Lexer
import xiaoLanguage.tokens.Keyword
import xiaoLanguage.tokens.Position
import xiaoLanguage.tokens.Token
import xiaoLanguage.tokens.TokenType
import xiaoLanguage.util.Report.*
import xiaoLanguage.util.Report
import java.io.File

class Parser(lex: Lexer, private val file: File) {
    private val parserReporter = mutableListOf<Report>()
    private var tokens: MutableList<Token> = mutableListOf()
    private val lexerReport = mutableListOf<Report>()
    private var ast = mutableListOf<ASTNode>()
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
        get() = index > tokens.size - 1

    private fun comparison(token: TokenType): Token = when {
        tokens[index].tokenType == token -> tokens[index++]
        tokens.isEmpty() -> {
            throw Exception()
        }
        else -> {
            parserReporter.add(Error(SyntaxError("Unexpected token ${tokens[index].tokenType}, expected token $token")))
            throw Exception()
        }
    }

    private fun expression() {
        while (!isEOFToken) {
            val node: ASTNode? = when (tokens[index].literal) {
                Keyword.CLASS_KEYWORD.keyword -> classExpression()
                Keyword.FUNCTION_KEYWORD.keyword -> functionExpression()
                Keyword.IMPORT_KEYWORD.keyword -> importExpression()
                else -> null
            }

            if (node != null) ast += node
            else break
        }

        println(ast)
    }

    private fun importExpression(): Import {
        val name = comparison(TokenType.IDENTIFIER_TOKEN)
        val lineNumber = name.position.lineNumber
        val path = mutableListOf<Token>()
        var isDot = false

        while (index < tokens.size && tokens[index].position.lineNumber == lineNumber) {
            if (!isDot) {
                isDot = true
                path += comparison(TokenType.IDENTIFIER_TOKEN)
            } else {
                comparison(TokenType.DOT_TOKEN)
                isDot = false
            }
        }

        if (!isDot) throw SyntaxError("invalid syntax")

        return Import(name, path)
    }

    private fun classExpression(): Class {
        val classKeyword = comparison(TokenType.IDENTIFIER_TOKEN)
        val className = comparison(TokenType.IDENTIFIER_TOKEN)
        val functions = mutableListOf<Function>()

        comparison(TokenType.LEFT_CURLY_BRACKETS_TOKEN)
        // TODO(parse functions)
        comparison(TokenType.RIGHT_CURLY_BRACKETS_TOKEN)

        return Class(classKeyword = classKeyword, className = className, functions = functions)
    }

    private fun functionExpression(): Function {
        val fnKeyword = comparison(TokenType.IDENTIFIER_TOKEN)
        val fnName = comparison(TokenType.IDENTIFIER_TOKEN)
        val parameters = mutableListOf<Parameter>()
        val statements = mutableListOf<Statement>()
        var colon: Token? = null
        var returnType: Token? = null
        var isComma = false

        comparison(TokenType.LEFT_PARENTHESES_TOKEN)

        while (!isEOFToken && tokens[index].tokenType != TokenType.RIGHT_PARENTHESES_TOKEN) {
            if (!isComma) {
                isComma = true
                parameters += parameterExpression()
            } else isComma = false
        }

        comparison(TokenType.RIGHT_PARENTHESES_TOKEN)

        if (tokens[index].tokenType == TokenType.COLON_TOKEN) {
            colon = comparison(TokenType.COLON_TOKEN)
            returnType = comparison(TokenType.IDENTIFIER_TOKEN)
        }

        comparison(TokenType.LEFT_CURLY_BRACKETS_TOKEN)
        // TODO()
        comparison(TokenType.RIGHT_CURLY_BRACKETS_TOKEN)
        return Function(
            functionKeyword = fnKeyword,
            functionName = fnName,
            parameters = parameters,
            colon = colon,
            returnType = returnType?.let { Type(it) },
            statements = statements
        )
    }

    private fun parameterExpression(): Parameter {
        val name = comparison(TokenType.IDENTIFIER_TOKEN)
        val colon = comparison(TokenType.COLON_TOKEN)
        val type = comparison(TokenType.IDENTIFIER_TOKEN)

        return Parameter(name, colon, Type(type))
    }
}
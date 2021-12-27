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
    private var ast = mutableListOf<Expression>()
    private var index = 0

    init {
        try {
            tokens = lex.lex()
        } catch (e: Exception) {
            lexerReport.add(Error(e, Position(lex.lineNumber, lex.exceptionIndex)))
        }
    }

    fun parser(): Pair<MutableList<Expression>, MutableList<Report>> {
        if (lexerReport.filterIsInstance<Error>().isEmpty()) {
            try {
                ast = expressions { true }
            } catch (e: Exception) {
                parserReporter += Error(SyntaxError(), tokens[index].position)
            }
        } else {
            lexerReport.forEach {
                it.printReport(file.readLines(), file.absolutePath)
            }
        }

        return ast to parserReporter
    }

    private val isEOFToken: Boolean
        get() = index > tokens.size - 1

    private fun comparison(token: TokenType): Token = when {
        tokens[index].tokenType == token -> tokens[index++]
        tokens.isEmpty() -> {
            throw Exception()
        }
        else -> {
            syntaxError(
                SyntaxError(
                    "Unexpected token ${tokens[index].tokenType}, expected token $token"
                ), tokens[index].position
            )

            throw Exception()
        }
    }

    private fun comparison(vararg token: TokenType): Token = when {
        tokens[index].tokenType in token -> tokens[index++]
        tokens.isEmpty() -> {
            throw Exception()
        }
        else -> {
            syntaxError(
                SyntaxError(
                    "Unexpected token ${tokens[index].tokenType}, expected token $token"
                ), tokens[index].position
            )

            throw Exception()
        }
    }

    private fun syntaxError(exception: Exception, position: Position) {
        parserReporter += Error(exception, position)

        throw exception
    }

    private fun expressions(determine: (MutableList<Expression>) -> Boolean): MutableList<Expression> {
        val nodes = mutableListOf<Expression>()

        while (!isEOFToken && determine(nodes)) {
            val node: Expression = when (tokens[index].literal) {
                Keyword.CLASS_KEYWORD.keyword -> classExpression()
                Keyword.FUNCTION_KEYWORD.keyword -> functionExpression()
                Keyword.IMPORT_KEYWORD.keyword -> importExpression()
                else -> expression()
            }

            nodes += node
        }

        return nodes
    }

    private fun expression(): Expression {
        val path = path()

        when (tokens[index].tokenType) {
            TokenType.LEFT_PARENTHESES_TOKEN -> {
                comparison(TokenType.LEFT_PARENTHESES_TOKEN)
                val args = mutableListOf<Token>()
                var isComma = false

                while (!isEOFToken) {
                    when (tokens[index].tokenType) {
                        TokenType.RIGHT_PARENTHESES_TOKEN -> {
                            comparison(TokenType.RIGHT_PARENTHESES_TOKEN)
                            break
                        }
                        TokenType.COMMA_TOKEN -> {
                            if (!isComma) syntaxError(SyntaxError(), tokens[index].position)
                            isComma = false
                        }
                        else -> {
                            if (isComma) syntaxError(SyntaxError(), tokens[index].position)
                            args += comparison(TokenType.IDENTIFIER_TOKEN, TokenType.INTEGER_LITERAL_TOKEN, TokenType.FLOAT_LITERAL_TOKEN)
                            isComma = true
                        }
                    }
                }
                return Expression.CallFunctionExpression(path, path[path.size - 1], args)
            }

            else -> return Expression.VariableExpression(path, path[path.size - 1])
        }
    }

    private fun path(): List<Token> {
        val lineNumber = tokens[index].position.lineNumber
        val path = mutableListOf<Token>()
        var isDot = false

        while (index < tokens.size && tokens[index].position.lineNumber == lineNumber) {
            if (!isDot) {
                isDot = true
                path += comparison(TokenType.IDENTIFIER_TOKEN)
            } else {
                if (tokens[index].tokenType == TokenType.DOT_TOKEN) {
                    comparison(TokenType.DOT_TOKEN)
                    isDot = false
                } else break
            }
        }

        if (!isDot) syntaxError(SyntaxError(), tokens[index].position)

        return path
    }

    private fun importExpression(): Import {
        val importKeyword = comparison(TokenType.IDENTIFIER_TOKEN)

        return Import(importKeyword, path())
    }

    private fun classExpression(): Class {
        val classKeyword = comparison(TokenType.IDENTIFIER_TOKEN)
        val className = comparison(TokenType.IDENTIFIER_TOKEN)
        val functions = mutableListOf<Function>()

        comparison(TokenType.LEFT_CURLY_BRACKETS_TOKEN)
        // TODO(parse functions)
        while (!isEOFToken && tokens[index].tokenType != TokenType.RIGHT_CURLY_BRACKETS_TOKEN) {
            when (tokens[index].literal) {
                Keyword.FUNCTION_KEYWORD.keyword -> functions += functionExpression()
                else -> parserReporter.add(Error(SyntaxError()))
            }
        }

        comparison(TokenType.RIGHT_CURLY_BRACKETS_TOKEN)

        return Class(classKeyword = classKeyword, className = className, functions = functions)
    }

    private fun functionExpression(): Function {
        val fnKeyword = comparison(TokenType.IDENTIFIER_TOKEN)
        val fnName = comparison(TokenType.IDENTIFIER_TOKEN)
        val parameters = mutableListOf<Parameter>()
        val statements = mutableListOf<Statement>()
        var colon: Token? = null
        var returnType: Expression.Type? = null
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
            returnType = typeExpression()
        }

        comparison(TokenType.LEFT_CURLY_BRACKETS_TOKEN)
        // TODO(parse statements)
        while (!isEOFToken && tokens[index].tokenType != TokenType.RIGHT_CURLY_BRACKETS_TOKEN) {
            statements += statementsExpression()
        }
        comparison(TokenType.RIGHT_CURLY_BRACKETS_TOKEN)

        return Function(
            functionKeyword = fnKeyword,
            functionName = fnName,
            parameters = parameters,
            colon = colon,
            returnType = returnType,
            statements = statements
        )
    }

    private fun parameterExpression(): Parameter {
        val name = comparison(TokenType.IDENTIFIER_TOKEN)
        val colon = comparison(TokenType.COLON_TOKEN)
        val type = typeExpression()

        return Parameter(name, colon, type)
    }

    private fun statementsExpression(): Statement {
        return when (tokens[index].literal) {
            Keyword.VARIABLE_KEYWORD.keyword -> variableDeclarationExpression()
            else -> Statement.ExpressionStatement(expression())
        }
    }

    private fun variableDeclarationExpression(): Statement.VariableDeclaration {
        val variableKeyword = comparison(TokenType.IDENTIFIER_TOKEN)
        val variableName = comparison(TokenType.IDENTIFIER_TOKEN)
        val expressions = mutableListOf<Expression>()
        var colon: Token? = null
        var variableType: Expression.Type? = null

        if (tokens[index].tokenType == TokenType.COLON_TOKEN) {
            colon = comparison(TokenType.COLON_TOKEN)
            variableType = typeExpression()
        }

        comparison(TokenType.EQUAL_TOKEN)

        while (tokens[index].tokenType in listOf(TokenType.PLUS_TOKEN)) {
            expressions += expressions {
                tokens[index].tokenType == TokenType.IDENTIFIER_TOKEN
            }[0]
        }

        return Statement.VariableDeclaration(
            variableKeyword,
            variableName,
            colon,
            variableType,
            Expression.OperatorExpression(expressions)
        )

    }

    private fun typeExpression(): Expression.Type {
        var isLeftSquareBrackets = true
        val typeTokens = mutableListOf(comparison(TokenType.IDENTIFIER_TOKEN))

        while (!isEOFToken) {
            when (tokens[index].tokenType) {
                TokenType.LEFT_SQUARE_BRACKETS_TOKEN -> {
                    if (isLeftSquareBrackets) {
                        typeTokens += comparison(TokenType.LEFT_SQUARE_BRACKETS_TOKEN)
                        isLeftSquareBrackets = false
                    } else syntaxError(SyntaxError(), tokens[index].position)
                }
                TokenType.RIGHT_SQUARE_BRACKETS_TOKEN -> {
                    if (!isLeftSquareBrackets) {
                        typeTokens += comparison(TokenType.RIGHT_SQUARE_BRACKETS_TOKEN)
                        isLeftSquareBrackets = true
                    } else syntaxError(SyntaxError(), tokens[index].position)
                }
                else -> {
                    if (!isLeftSquareBrackets) syntaxError(SyntaxError(), tokens[index - 1].position)
                    break
                }
            }
        }

        return Expression.Type(typeTokens)
    }
}
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

    fun parser(): Pair<MutableList<ASTNode>, MutableList<Report>> {
        if (lexerReport.filterIsInstance<Error>().isEmpty()) {
            try {
                ast = expressions { true }
            } catch (e: Exception) {
                if (e !is SyntaxError)
                    parserReporter += Error(e, currently?.position)
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

    private val currently: Token?
        get() = if (isEOFToken) null else tokens[index]

    private fun comparison(token: TokenType): Token = when {
        currently?.tokenType == token -> tokens[index++]
        tokens.isEmpty() -> {
            throw Exception()
        }
        else -> {
            syntaxError(
                SyntaxError(
                    "Unexpected token ${currently?.tokenType}, expected token $token"
                ), currently?.position
            )

            throw Exception()
        }
    }

    private fun comparison(vararg token: TokenType): Token = when {
        currently?.tokenType in token -> tokens[index++]
        tokens.isEmpty() -> {
            throw Exception()
        }
        else -> {
            syntaxError(
                SyntaxError(
                    "Unexpected token ${currently?.tokenType}, expected token $token"
                ), currently?.position
            )

            throw Exception()
        }
    }

    private fun syntaxError(exception: Exception, position: Position?) {
        parserReporter += Error(exception, position)

        throw exception
    }

    private fun expressions(determine: (MutableList<ASTNode>) -> Boolean): MutableList<ASTNode> {
        val nodes = mutableListOf<ASTNode>()

        while (!isEOFToken && determine(nodes)) {
            val node: ASTNode = when (currently?.literal) {
                Keyword.CLASS_KEYWORD.keyword -> classExpression()
                Keyword.FUNCTION_KEYWORD.keyword -> functionExpression()
                Keyword.IMPORT_KEYWORD.keyword -> importExpression()
                else -> statementsExpression()
            }

            nodes += node
        }

        return nodes
    }

    private fun expression(): Expression {
        val path = if (currently?.tokenType == TokenType.IDENTIFIER_TOKEN) path()
        else listOf(
            comparison(
                TokenType.STRING_LITERAL_TOKEN,
                TokenType.FLOAT_LITERAL_TOKEN,
                TokenType.INTEGER_LITERAL_TOKEN
            )
        )

        when (currently?.tokenType) {
            TokenType.LEFT_PARENTHESES_TOKEN -> {
                comparison(TokenType.LEFT_PARENTHESES_TOKEN)
                val args = mutableListOf<Expression>()
                var isComma = false

                while (!isEOFToken) {
                    when (currently?.tokenType) {
                        TokenType.RIGHT_PARENTHESES_TOKEN -> {
                            comparison(TokenType.RIGHT_PARENTHESES_TOKEN)
                            break
                        }
                        TokenType.COMMA_TOKEN -> {
                            if (!isComma) syntaxError(SyntaxError(), currently?.position)
                            isComma = false
                        }
                        else -> {
                            if (isComma) syntaxError(SyntaxError(), currently?.position)
                            args += expression()
                            isComma = true
                        }
                    }
                }

                return Expression.CallFunctionExpression(path, path[path.size - 1], args)
            }

            else -> {
                return when (path[0].tokenType) {
                    TokenType.INTEGER_LITERAL_TOKEN -> Expression.IntExpression(path[0])
                    TokenType.STRING_LITERAL_TOKEN -> Expression.StringExpression(path[0])
                    TokenType.FLOAT_LITERAL_TOKEN -> Expression.FloatExpression(path[0])
                    else -> Expression.VariableExpression(path, path[path.size - 1])
                }
            }
        }
    }

    /**
     * parse parameter
     * example "**a: Str**"
     * @return Parameter data class
     */
    private fun parameterExpression(): Parameter {
        val name = comparison(TokenType.IDENTIFIER_TOKEN)
        val colon = comparison(TokenType.COLON_TOKEN)
        val type = typeExpression()

        return Parameter(name, colon, type)
    }

    private fun statementsExpression(): Statement {
        return when (currently?.literal) {
            Keyword.VARIABLE_KEYWORD.keyword -> variableDeclarationExpression()
            Keyword.IF_KEYWORD.keyword -> ifStatementExpression()
            Keyword.RETURN_KEYWORD.keyword -> returnStatementExpression()
            else -> Statement.ExpressionStatement(expression())
        }
    }

    /**
     * parse path or value
     * example "**a.b.c**" or Int or String or Float
     * @return Token list
     */
    private fun path(): List<Token> {
        val lineNumber = currently?.position?.lineNumber
        val path = mutableListOf<Token>()
        var isDot = false

        if (lineNumber == null) syntaxError(SyntaxError(), tokens[index - 1].position)

        while (!isEOFToken) {
            if (!isDot) {
                isDot = true
                path += comparison(TokenType.IDENTIFIER_TOKEN)
            } else {
                if (currently?.tokenType == TokenType.DOT_TOKEN) {
                    comparison(TokenType.DOT_TOKEN)
                    isDot = false
                } else break
            }
        }

        if (!isDot) syntaxError(SyntaxError(), currently?.position)

        return path
    }

    /**
     * parse import
     * example "**im xiao.Math**"
     * @return Import data class
     */
    private fun importExpression(): Import {
        val importKeyword = comparison(TokenType.IDENTIFIER_TOKEN)

        return Import(importKeyword, path())
    }

    /**
     * parse class
     * example "**class A {...}**"
     * @return Parameter data class
     */
    private fun classExpression(): Class {
        val classKeyword = comparison(TokenType.IDENTIFIER_TOKEN)
        val className = comparison(TokenType.IDENTIFIER_TOKEN)
        val functions = mutableListOf<Function>()

        comparison(TokenType.LEFT_CURLY_BRACKETS_TOKEN)

        while (!isEOFToken && currently?.tokenType != TokenType.RIGHT_CURLY_BRACKETS_TOKEN) {
            when (currently?.literal) {
                Keyword.FUNCTION_KEYWORD.keyword -> functions += functionExpression()
                else -> parserReporter.add(Error(SyntaxError()))
            }
        }

        comparison(TokenType.RIGHT_CURLY_BRACKETS_TOKEN)

        return Class(classKeyword = classKeyword, className = className, functions = functions)
    }

    /**
     * parse function
     * example "**fn a(a: Str, ...) {...}**"
     * @return Function data class
     */
    private fun functionExpression(): Function {
        val fnKeyword = comparison(TokenType.IDENTIFIER_TOKEN)
        val fnName = comparison(TokenType.IDENTIFIER_TOKEN)
        val parameters = mutableListOf<Parameter>()
        val statements = mutableListOf<Statement>()
        var colon: Token? = null
        var returnType: Type? = null
        var isComma = false

        comparison(TokenType.LEFT_PARENTHESES_TOKEN)

        while (!isEOFToken && currently?.tokenType != TokenType.RIGHT_PARENTHESES_TOKEN) {
            if (!isComma) {
                isComma = true
                parameters += parameterExpression()
            } else {
                comparison(TokenType.COMMA_TOKEN)
                isComma = false
            }
        }

        comparison(TokenType.RIGHT_PARENTHESES_TOKEN)

        if (currently?.tokenType == TokenType.COLON_TOKEN) {
            colon = comparison(TokenType.COLON_TOKEN)
            returnType = typeExpression()
        }

        comparison(TokenType.LEFT_CURLY_BRACKETS_TOKEN)

        while (!isEOFToken && currently?.tokenType != TokenType.RIGHT_CURLY_BRACKETS_TOKEN) {
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

    /**
     * parse variable declaration
     * example "**var a = 10**"
     * @return Variable declaration data class
     */
    private fun variableDeclarationExpression(): Statement.VariableDeclaration {
        val variableKeyword = comparison(TokenType.IDENTIFIER_TOKEN)
        val variableName = comparison(TokenType.IDENTIFIER_TOKEN)
        var colon: Token? = null

        var variableType: Type? = null

        if (currently?.tokenType == TokenType.COLON_TOKEN) {
            colon = comparison(TokenType.COLON_TOKEN)
            variableType = typeExpression()
        }

        comparison(TokenType.EQUAL_TOKEN)

        val value = operatorExpression()

        return Statement.VariableDeclaration(
            variableKeyword,
            variableName,
            colon,
            variableType,
            value
        )
    }

    private fun ifStatementExpression(): Statement.IfStatement {
        val ifStatementKeyword = comparison(TokenType.IDENTIFIER_TOKEN)
        val statements = mutableListOf<Statement>()
        val elseStatement = mutableListOf<Statement.ElseStatement>()
        var conditional: Expression? = null
        comparison(TokenType.LEFT_PARENTHESES_TOKEN)

        while (!isEOFToken) {
            when (currently?.tokenType) {
                TokenType.RIGHT_PARENTHESES_TOKEN -> break
                else -> conditional = operatorExpression()
            }
        }

        comparison(TokenType.LEFT_CURLY_BRACKETS_TOKEN)

        while (!isEOFToken) {
            when (currently?.tokenType) {
                TokenType.RIGHT_CURLY_BRACKETS_TOKEN -> break
                else -> statements += statementsExpression()
            }
        }
        comparison(TokenType.RIGHT_CURLY_BRACKETS_TOKEN)

        while (!isEOFToken) {
            when (currently?.tokenType) {
                TokenType.IDENTIFIER_TOKEN -> {
                    if (currently?.literal == Keyword.ELSE_KEYWORD.keyword) {
                        elseStatement += elseStatementExpression()
                    } else break
                }
                else -> break
            }
        }

        return Statement.IfStatement(
            ifStatementKeyword,
            conditional,
            statements,
            elseStatement,
            ifStatementKeyword.position
        )
    }

    private fun elseStatementExpression(): Statement.ElseStatement {
        val elseKeyword = comparison(TokenType.IDENTIFIER_TOKEN)
        val statements = mutableListOf<Statement>()
        var ifKeyword: Token? = null
        var conditional: Expression? = null

        if (currently?.tokenType == TokenType.IDENTIFIER_TOKEN) {
            if (currently?.literal == Keyword.IF_KEYWORD.keyword) {
                ifKeyword = comparison(TokenType.IDENTIFIER_TOKEN)
                comparison(TokenType.LEFT_PARENTHESES_TOKEN)

                while (!isEOFToken) {
                    when (currently?.tokenType) {
                        TokenType.RIGHT_PARENTHESES_TOKEN -> break
                        else -> conditional = expression()
                    }
                }
            } else syntaxError(SyntaxError(), currently?.position)
        }

        comparison(TokenType.LEFT_CURLY_BRACKETS_TOKEN)

        while (!isEOFToken) {
            when (currently?.tokenType) {
                TokenType.RIGHT_CURLY_BRACKETS_TOKEN -> break
                else -> statements += statementsExpression()
            }
        }

        return Statement.ElseStatement(elseKeyword, ifKeyword, conditional, statements)
    }

    private fun returnStatementExpression(): Statement.ReturnStatement {
        val returnKeyword = comparison(TokenType.IDENTIFIER_TOKEN)
        val expression = expression()

        return Statement.ReturnStatement(returnKeyword, expression)
    }

    private fun operatorExpression(): Expression {
        val expressions = mutableListOf<Expression>()
        var operator: Token? = null
        var brackets = false

        if (currently?.tokenType == TokenType.LEFT_PARENTHESES_TOKEN) {
            comparison(TokenType.LEFT_PARENTHESES_TOKEN)
            brackets = true
        }

        while (!isEOFToken) {
            when (currently?.tokenType) {
                TokenType.IDENTIFIER_TOKEN, TokenType.INTEGER_LITERAL_TOKEN, TokenType.FLOAT_LITERAL_TOKEN, TokenType.STRING_LITERAL_TOKEN -> {
                    if (expressions.size != 0 && operator == null) {
                        if (brackets) syntaxError(SyntaxError(), currently?.position)
                        if (expressions[0].position?.lineNumber != currently!!.position.lineNumber) break
                        else syntaxError(SyntaxError(), currently?.position)
                    }

                    expressions += expression()
                }
                TokenType.LEFT_PARENTHESES_TOKEN -> operatorExpression()
                TokenType.RIGHT_PARENTHESES_TOKEN -> {
                    if (brackets) {
                        comparison(TokenType.RIGHT_PARENTHESES_TOKEN)
                        break
                    } else syntaxError(SyntaxError(), currently?.position)
                }
                else -> {
                    if (currently?.tokenType !in listOf(
                            TokenType.MINUS_TOKEN,
                            TokenType.MULTIPLY_TOKEN,
                            TokenType.PLUS_TOKEN,
                            TokenType.SLASH_TOKEN
                        )
                    ) break
                    operator = currently
                }
            }
        }

        return when (operator?.tokenType) {
            TokenType.PLUS_TOKEN -> Expression.OperatorExpression(Operator.Plus(operator, expressions))
            else -> expressions[0]
        }
    }

    private fun typeExpression(): Type {
        var isLeftSquareBrackets = true
        val typeToken = comparison(TokenType.IDENTIFIER_TOKEN)
        var array = 0

        while (!isEOFToken) {
            when (currently?.tokenType) {
                TokenType.LEFT_SQUARE_BRACKETS_TOKEN -> {
                    if (isLeftSquareBrackets) {
                        comparison(TokenType.LEFT_SQUARE_BRACKETS_TOKEN)
                        array++
                        isLeftSquareBrackets = false
                    } else syntaxError(SyntaxError(), currently?.position)
                }
                TokenType.RIGHT_SQUARE_BRACKETS_TOKEN -> {
                    if (!isLeftSquareBrackets) {
                        comparison(TokenType.RIGHT_SQUARE_BRACKETS_TOKEN)
                        isLeftSquareBrackets = true
                    } else syntaxError(SyntaxError(), currently?.position)
                }
                else -> {
                    if (!isLeftSquareBrackets) syntaxError(SyntaxError(), tokens[index - 1].position)
                    break
                }
            }
        }

        return Type(typeToken, array)
    }
}
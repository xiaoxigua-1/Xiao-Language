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
            lexerReport.add(Error(e, Code(lex.lineNumber, Position(lex.lineNumber, lex.exceptionIndex))))
        }
    }

    fun parser(): Pair<MutableList<ASTNode>, MutableList<Report>> {
        if (lexerReport.filterIsInstance<Error>().isEmpty()) {
            try {
                ast = expressions { true }
            } catch (e: Exception) {
                if (e !is SyntaxError)
                    parserReporter += Error(
                        e,
                        if (currently == null) null else Code(currently!!.position.lineNumber, currently!!.position)
                    )
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
        parserReporter += Error(exception, if (position == null) null else Code(position.lineNumber, position))

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

    /**
     * parse expressions
     * example all expression
     * @return expression data class
     */
    private fun expression(isValue: Boolean = false): Expression {
        if (tokens.size < index + 2) return valueExpress()
        return when (tokens[index + 1].tokenType) {
            TokenType.LEFT_PARENTHESES_TOKEN -> callFunction()
            TokenType.MINUS_TOKEN,
            TokenType.MULTIPLY_TOKEN,
            TokenType.PLUS_TOKEN,
            TokenType.SLASH_TOKEN,
            TokenType.MORE_TOKEN,
            TokenType.LESS_TOKEN -> if (!isValue) operatorExpression() else valueExpress()
            TokenType.ARROW_TOKEN -> generatorExpress()
            TokenType.EQUAL_TOKEN -> reSetVariableExpression()
            else -> valueExpress()
        }
    }

    /**
     * parse value
     * example string or int or float
     * @return expression data class
     */
    private fun valueExpress(): Expression {
        val value = comparison(
            TokenType.INTEGER_LITERAL_TOKEN,
            TokenType.FLOAT_LITERAL_TOKEN,
            TokenType.STRING_LITERAL_TOKEN,
            TokenType.IDENTIFIER_TOKEN
        )

        return when (value.tokenType) {
            TokenType.STRING_LITERAL_TOKEN -> Expression.StringExpression(value)
            TokenType.FLOAT_LITERAL_TOKEN -> Expression.FloatExpression(value)
            TokenType.INTEGER_LITERAL_TOKEN -> Expression.IntExpression(value)
            else -> Expression.VariableExpression(value)
        }
    }

    /**
     * parse generator
     * example "0->10"
     * @return generator data class
     */
    private fun generatorExpress(): Expression.GeneratorExpression {
        val value = mutableListOf<Token>()

        value += comparison(
            TokenType.INTEGER_LITERAL_TOKEN,
            TokenType.FLOAT_LITERAL_TOKEN,
            TokenType.STRING_LITERAL_TOKEN,
            TokenType.IDENTIFIER_TOKEN
        )

        comparison(TokenType.ARROW_TOKEN)

        value += comparison(
            TokenType.INTEGER_LITERAL_TOKEN,
            TokenType.FLOAT_LITERAL_TOKEN,
            TokenType.STRING_LITERAL_TOKEN,
            TokenType.IDENTIFIER_TOKEN
        )

        return Expression.GeneratorExpression(value)
    }

    private fun reSetVariableExpression(): Expression.ReSetVariableExpression {
        val variableToken = comparison(TokenType.IDENTIFIER_TOKEN)
        comparison(TokenType.EQUAL_TOKEN)

        return Expression.ReSetVariableExpression(variableToken, path())
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

    /**
     * parse statements
     * var or if or return or expression
     * @return statements data class
     */
    private fun statementsExpression(): Statement {
        return when (currently?.literal) {
            Keyword.VARIABLE_KEYWORD.keyword -> variableDeclarationExpression()
            Keyword.IF_KEYWORD.keyword -> ifStatementExpression()
            Keyword.RETURN_KEYWORD.keyword -> returnStatementExpression()
            Keyword.FOR_KEYWORD.keyword -> forStatementExpression()
            else -> Statement.ExpressionStatement(path())
        }
    }

    /**
     * parse path or value
     * example "**a.b().c()**"
     * @return Token list
     */
    private fun path(): List<Expression> {
        val lineNumber = currently?.position?.lineNumber
        val path = mutableListOf<Expression>()
        var isDot = false

        if (lineNumber == null) syntaxError(SyntaxError(), tokens[index - 1].position)

        while (!isEOFToken) {
            if (!isDot) {
                isDot = true
                path += expression()
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
        val lineNumber = importKeyword.position.lineNumber
        val path = mutableListOf<Token>()
        var isDot = false

        while (!isEOFToken) {
            if (!isDot) {
                if (currently?.position?.lineNumber != lineNumber) syntaxError(
                    SyntaxError("import must be placed on a single line"),
                    tokens[index - 1].position
                )
                path += comparison(TokenType.IDENTIFIER_TOKEN)
                isDot = true
            } else {
                if (currently?.tokenType == TokenType.DOT_TOKEN) {
                    comparison(TokenType.DOT_TOKEN)
                    isDot = false
                } else if (lineNumber == currently?.position?.lineNumber) syntaxError(
                    SyntaxError(),
                    currently?.position
                )
                else break
            }
        }

        if (!isDot) syntaxError(SyntaxError(), currently?.position)

        return Import(importKeyword, path)
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
        val variables = mutableListOf<Statement.VariableDeclaration>()

        comparison(TokenType.LEFT_CURLY_BRACKETS_TOKEN)

        while (!isEOFToken && currently?.tokenType != TokenType.RIGHT_CURLY_BRACKETS_TOKEN) {
            when (currently?.literal) {
                Keyword.FUNCTION_KEYWORD.keyword -> functions += functionExpression()
                Keyword.VARIABLE_KEYWORD.keyword -> variables += variableDeclarationExpression()
                else -> parserReporter.add(Error(SyntaxError()))
            }
        }

        comparison(TokenType.RIGHT_CURLY_BRACKETS_TOKEN)

        return Class(classKeyword, className, functions, variables)
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
        val statements = mutableListOf<ASTNode>()
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

        expressions { currently?.tokenType != TokenType.RIGHT_CURLY_BRACKETS_TOKEN }.forEach {
            statements += it
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
        val mutKeyword: Token? =
            if (currently?.tokenType == TokenType.IDENTIFIER_TOKEN && currently?.literal == Keyword.MUT_KEYWORD.keyword) comparison(
                TokenType.IDENTIFIER_TOKEN
            ) else null
        val variableName = comparison(TokenType.IDENTIFIER_TOKEN)
        var colon: Token? = null

        var variableType: Type? = null

        if (currently?.tokenType == TokenType.COLON_TOKEN) {
            colon = comparison(TokenType.COLON_TOKEN)
            variableType = typeExpression()
        }

        comparison(TokenType.EQUAL_TOKEN)

        val value = expression()

        return Statement.VariableDeclaration(
            variableKeyword,
            mutKeyword,
            variableName,
            colon,
            variableType,
            value
        )
    }

    /**
     * parse if statement
     * example "**if (...) {...}**"
     * @return If statement data class
     */
    private fun ifStatementExpression(): Statement.IfStatement {
        val ifStatementKeyword = comparison(TokenType.IDENTIFIER_TOKEN)
        val statements = mutableListOf<Statement>()
        val elseStatements = mutableListOf<Statement.ElseStatement>()
        var conditional: Expression? = null
        var isElse = false

        comparison(TokenType.LEFT_PARENTHESES_TOKEN)

        while (!isEOFToken) {
            when (currently?.tokenType) {
                TokenType.RIGHT_PARENTHESES_TOKEN -> break
                else -> conditional = operatorExpression()
            }
        }

        comparison(TokenType.RIGHT_PARENTHESES_TOKEN)
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
                        val elseKeyword = currently
                        val elseStatement = elseStatementExpression()
                        if (isElse) syntaxError(SyntaxError(), elseKeyword?.position)
                        isElse = elseStatement.ifKeyword == null
                        elseStatements += elseStatement
                    } else break
                }
                else -> break
            }
        }

        return Statement.IfStatement(
            ifStatementKeyword,
            conditional,
            statements,
            elseStatements,
            ifStatementKeyword.position
        )
    }

    /**
     * parse if statement
     * example "**else (...) {...}**"
     * @return If statement data class
     */
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
                comparison(TokenType.RIGHT_PARENTHESES_TOKEN)
            } else syntaxError(SyntaxError(), currently?.position)
        }

        comparison(TokenType.LEFT_CURLY_BRACKETS_TOKEN)

        while (!isEOFToken) {
            when (currently?.tokenType) {
                TokenType.RIGHT_CURLY_BRACKETS_TOKEN -> break
                else -> statements += statementsExpression()
            }
        }

        comparison(TokenType.RIGHT_CURLY_BRACKETS_TOKEN)

        return Statement.ElseStatement(elseKeyword, ifKeyword, conditional, statements)
    }

    /**
     * parse for statement
     * example "**for(...){...}**"
     * @return for statement data class
     */
    private fun forStatementExpression(): Statement.ForStatement {
        val forKeyword = comparison(TokenType.IDENTIFIER_TOKEN)

        comparison(TokenType.LEFT_PARENTHESES_TOKEN)

        val variable = comparison(TokenType.IDENTIFIER_TOKEN)
        val inKeyword = comparison(TokenType.IDENTIFIER_TOKEN)
        val expression = expression()
        val statements = mutableListOf<Statement>()

        if (inKeyword.literal != Keyword.IN_KEYWORD.keyword) syntaxError(SyntaxError(), inKeyword.position)

        comparison(TokenType.RIGHT_PARENTHESES_TOKEN)
        comparison(TokenType.LEFT_CURLY_BRACKETS_TOKEN)

        while (!isEOFToken) {
            when (currently?.tokenType) {
                TokenType.RIGHT_CURLY_BRACKETS_TOKEN -> {
                    comparison(TokenType.RIGHT_CURLY_BRACKETS_TOKEN)
                    break
                }
                else -> statements += statementsExpression()
            }
        }

        return Statement.ForStatement(forKeyword, variable, inKeyword, expression, statements)
    }

    /**
     * parse return statement
     * example "**return ...**"
     * @return return statement data class
     */
    private fun returnStatementExpression(): Statement.ReturnStatement {
        val returnKeyword = comparison(TokenType.IDENTIFIER_TOKEN)
        val expression = expression()

        return Statement.ReturnStatement(returnKeyword, expression)
    }

    /**
     * parse call function
     * example "**test()**"
     * @return return statement data class
     */
    private fun callFunction(): Expression.CallExpression {
        val functionName = comparison(TokenType.IDENTIFIER_TOKEN)
        val args = mutableListOf<Expression>()
        var isComma = false

        comparison(TokenType.LEFT_PARENTHESES_TOKEN)

        while (!isEOFToken) {
            when (currently?.tokenType) {
                TokenType.RIGHT_PARENTHESES_TOKEN -> {
                    comparison(TokenType.RIGHT_PARENTHESES_TOKEN)
                    break
                }
                TokenType.COMMA_TOKEN -> {
                    if (!isComma) syntaxError(SyntaxError(), currently?.position)
                    comparison(TokenType.COMMA_TOKEN)
                    isComma = false
                }
                else -> {
                    if (isComma) syntaxError(SyntaxError(), currently?.position)
                    args += expression()
                    isComma = true
                }
            }
        }
        return Expression.CallExpression(functionName, args)
    }

    // TODO bug: operator precedence
    /**
     * parse operator
     * example "**10 * 10**"
     * @return operator data class
     */
    private fun operatorExpression(expression: Expression? = null): Expression {
        val expressions = mutableListOf<Expression>()
        var operator: Token? = null
        var brackets = false

        if (expression != null) expressions += expression

        if (currently?.tokenType == TokenType.LEFT_PARENTHESES_TOKEN) {
            comparison(TokenType.LEFT_PARENTHESES_TOKEN)
            brackets = true
        }

        while (!isEOFToken) {
            when (currently?.tokenType) {
                TokenType.IDENTIFIER_TOKEN,
                TokenType.INTEGER_LITERAL_TOKEN,
                TokenType.FLOAT_LITERAL_TOKEN,
                TokenType.STRING_LITERAL_TOKEN -> {
                    if (expressions.size != 0 && operator == null) {
                        if (brackets) syntaxError(SyntaxError(), currently?.position)
                        if (expressions[0].position?.lineNumber != currently!!.position.lineNumber) break
                        else syntaxError(SyntaxError(), currently?.position)
                    }

                    expressions += expression(true)
                }
                TokenType.LEFT_PARENTHESES_TOKEN -> expressions += operatorExpression()
                TokenType.RIGHT_PARENTHESES_TOKEN -> {
                    if (brackets) {
                        comparison(TokenType.RIGHT_PARENTHESES_TOKEN)
                        break
                    } else break
                }
                else -> {
                    if (currently?.tokenType !in listOf(
                            TokenType.MINUS_TOKEN,
                            TokenType.MULTIPLY_TOKEN,
                            TokenType.PLUS_TOKEN,
                            TokenType.SLASH_TOKEN,
                            TokenType.MORE_TOKEN,
                            TokenType.LESS_TOKEN
                        )
                    ) break

                    operator = comparison(
                        TokenType.MINUS_TOKEN,
                        TokenType.MULTIPLY_TOKEN,
                        TokenType.PLUS_TOKEN,
                        TokenType.SLASH_TOKEN,
                        TokenType.MORE_TOKEN,
                        TokenType.LESS_TOKEN
                    )
                }
            }
        }

        return when (operator?.tokenType) {
            TokenType.PLUS_TOKEN -> Expression.OperatorExpression(Operator.Plus(operator, expressions))
            TokenType.MULTIPLY_TOKEN -> Expression.OperatorExpression(Operator.Multiplied(operator, expressions))
            TokenType.MINUS_TOKEN -> Expression.OperatorExpression(Operator.Minus(operator, expressions))
            TokenType.LESS_TOKEN -> Expression.OperatorExpression(Operator.Less(operator, expressions))
            TokenType.MORE_TOKEN -> Expression.OperatorExpression(Operator.More(operator, expressions))
            else -> expressions[0]
        }
    }

    /**
     * parse type
     * example "**Int[]** or **Int**"
     * @return type data class
     */
    private fun typeExpression(): Type {
        var isLeftSquareBrackets = true
        val typeTokens = mutableListOf(comparison(TokenType.IDENTIFIER_TOKEN))
        var array = 0

        while (!isEOFToken) {
            when (currently?.tokenType) {
                TokenType.LEFT_SQUARE_BRACKETS_TOKEN -> {
                    if (isLeftSquareBrackets) {
                        typeTokens += comparison(TokenType.LEFT_SQUARE_BRACKETS_TOKEN)
                        array++
                        isLeftSquareBrackets = false
                    } else syntaxError(SyntaxError(), currently?.position)
                }
                TokenType.RIGHT_SQUARE_BRACKETS_TOKEN -> {
                    if (!isLeftSquareBrackets) {
                        typeTokens += comparison(TokenType.RIGHT_SQUARE_BRACKETS_TOKEN)
                        isLeftSquareBrackets = true
                    } else syntaxError(SyntaxError(), currently?.position)
                }
                else -> {
                    if (!isLeftSquareBrackets) syntaxError(SyntaxError(), tokens[index - 1].position)
                    break
                }
            }
        }

        return Type(typeTokens, array, typeTokens[0].literal)
    }
}
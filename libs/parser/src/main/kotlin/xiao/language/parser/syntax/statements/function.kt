package xiao.language.parser.syntax.statements

import xiao.language.parser.Parser
import xiao.language.parser.expect
import xiao.language.parser.syntax.expressions
import xiao.language.utilities.Span
import xiao.language.utilities.Token
import xiao.language.utilities.ast.*
import xiao.language.utilities.exceptions.Exceptions.ExpectException
import xiao.language.utilities.tokens.Delimiters
import xiao.language.utilities.tokens.Punctuations
import xiao.language.utilities.tokens.Tokens

/**
 * parser function statement
 * ### Example
 * ```
 * fn test() {
 *     println("");
 * }
 *
 * fn Test::test() {
 *     println("");
 * }
 *
 * fn test() = println("");
 */
fun Parser.function(vis: Visibility, kwd: Token): Statement.Function {
    val name = functionName(kwd.span)
    val params = parameter(name.span)
    val returnType = functionReturnType()
    val block = functionBlock(params.right.span)
    return Statement.Function(vis, kwd, name, params, returnType, block)
}

/**
 * parse function **name**  is path or identifier
 * ### **Example**
 *  - Test::test
 *  - test
 */
fun Parser.functionName(span: Span): Expressions {
    val token = expect(Tokens.Identifier, ExpectException("Expect identifier", span))

    return when (lexer.peek().type) {
        Tokens.Punctuation(Punctuations.PathSep) -> {
            val pathSep = lexer.next()
            Expressions.Identifier(
                token,
                Expressions.Identifier(
                    expect(Tokens.Identifier, ExpectException("Expect identifier", pathSep.span))
                )
            )
        }

        else -> Expressions.Identifier(token, span = token.span)
    }
}

/**
 * parse function parameters
 *
 * ### **Example:**
 * ```
 *  (test: Test, test2: Test2)
 * ```
 */
fun Parser.parameter(span: Span): Parameters {
    val left = expect(
        Tokens.Delimiter(Delimiters.LeftParentheses), ExpectException("Missing left parentheses.", span)
    )
    val params = mutableListOf<Parameter>()
    var comma: Token? = null

    for (token in lexer) {
        val type = token.type
        comma = when {
            type is Tokens.Delimiter && type.type == Delimiters.RightParentheses -> return Parameters(
                left, params, token
            )

            type is Tokens.Punctuation && type.punctuation == Punctuations.Comma && comma == null -> token
            else -> {
                val name = expect(Tokens.Identifier, ExpectException("Missing parameter name.", comma!!.span))
                val colon = expect(Tokens.Punctuation(Punctuations.Colon), ExpectException("Missing Colon.", name.span))
                val parameterType = expressions()

                params.add(Parameter(name, colon, parameterType))
                null
            }
        }
    }

    throw ExpectException("Unclosed delimiter", left.span)
}

fun Parser.functionReturnType(): Expressions? {
    while (lexer.hasNext()) {
        val peekToken = lexer.peek()

        when (peekToken.type) {
            Tokens.Whitespace, Tokens.NewLine -> lexer.next()
            Tokens.Punctuation(Punctuations.Colon) -> {
                expect(Tokens.Punctuation(Punctuations.Colon), ExpectException("Expect `:`", peekToken.span))
                return expressions()
            }

            else -> break
        }
    }

    return null
}

fun Parser.functionBlock(span: Span): Expressions {
    while (lexer.hasNext()) {
        val peekToken = lexer.peek()

        when (peekToken.type) {
            Tokens.Whitespace, Tokens.NewLine -> lexer.next()
            Tokens.Punctuation(Punctuations.Eq) -> {
                expect(Tokens.Punctuation(Punctuations.Eq), ExpectException("Expect `=`", span))
                return expressions()
            }
            else -> return expressions()
        }
    }

    throw ExpectException("Block expression", span)
}
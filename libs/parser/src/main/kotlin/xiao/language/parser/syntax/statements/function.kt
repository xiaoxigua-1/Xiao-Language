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

fun Parser.function(vis: Visibility, kwd: Token): Statement.Function {
    val name = functionName(kwd.span)
    val params = parameter(name.span)
    val block = functionBlock(params.right.span)
    return Statement.Function(vis, kwd, name, params, block)
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
            expect(Tokens.Punctuation(Punctuations.PathSep), ExpectException("", token.span))
            Expressions.Path(Expressions.Identifier(token), Expressions.Identifier(lexer.next()))
        }

        else -> Expressions.Identifier(token, token.span)
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

fun Parser.functionBlock(span: Span): Expressions {
    when (val expression = expressions()) {
        is Expressions.Block, is Expressions.EqValue -> return expression
        else -> throw ExpectException("Expect Block", span)
    }
}
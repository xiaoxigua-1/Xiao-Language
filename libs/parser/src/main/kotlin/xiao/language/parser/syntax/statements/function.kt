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
    val name = expressions()
    val params = parameter(name.span)
    val blockExpression = expressions()
    val block =
        if (blockExpression is Expressions.Block) blockExpression else throw ExpectException("Block", params.right.span)
    return Statement.Function(vis, kwd, name, params, block, Span(kwd.span.start, block.span.end))
}

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
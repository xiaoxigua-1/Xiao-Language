package xiao.language.parser.syntax

import xiao.language.parser.Parser
import xiao.language.parser.expect
import xiao.language.utilities.Span
import xiao.language.utilities.Token
import xiao.language.utilities.ast.Parameter
import xiao.language.utilities.ast.Statement
import xiao.language.utilities.ast.Visibility
import xiao.language.utilities.exceptions.Exceptions.ExpectException
import xiao.language.utilities.tokens.Delimiters
import xiao.language.utilities.tokens.Punctuations
import xiao.language.utilities.tokens.Tokens

fun Parser.function(vis: Visibility, kwd: Token) {
    val name = expect(Tokens.Identifier, ExpectException("Missing function name.", kwd.span))
    val params = parameter(name.span)
    Statement.Function(vis, kwd, name, params)
}

fun Parser.parameter(span: Span): List<Parameter> {
    val left = expect(Tokens.Delimiter(Delimiters.LeftParentheses), ExpectException("Missing left parentheses.", span))
    val params = mutableListOf<Parameter>()
    var comma: Token? = null

    for (token in lexer) {
        val type = token.type
        comma = when {
            type is Tokens.Delimiter && type.type == Delimiters.RightParentheses -> return params
            type is Tokens.Punctuation && type.punctuation == Punctuations.Comma && comma == null -> token
            else -> {
                val name = expect(Tokens.Identifier, ExpectException("Missing parameter name.", comma!!.span))
                val colon = expect(Tokens.Punctuation(Punctuations.Colon), ExpectException("Missing Colon.", name.span))
                val parameterType = expect(Tokens.Identifier, ExpectException("Missing parameter type.", colon.span))

                params.add(Parameter(name, colon, parameterType))
                null
            }
        }
    }

    throw ExpectException("Unclosed delimiter", left.span)
}
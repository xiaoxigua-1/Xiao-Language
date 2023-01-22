package xiao.language.parser.syntax

import xiao.language.parser.Parser
import xiao.language.parser.expect
import xiao.language.utilities.Token
import xiao.language.utilities.ast.Parameter
import xiao.language.utilities.ast.Statement
import xiao.language.utilities.ast.Visibility
import xiao.language.utilities.exceptions.ExpectException
import xiao.language.utilities.tokens.Delimiters
import xiao.language.utilities.tokens.Punctuations
import xiao.language.utilities.tokens.Tokens

fun Parser.function(vis: Visibility, kwd: Token) {
    val name = expect(Tokens.Identifier, ExpectException("Missing function name.", "Identifier"))
    val params = parameter()
    Statement.Function(vis, kwd, name, params)
}

fun Parser.parameter(): List<Parameter> {
    val left = expect(Tokens.Delimiter(Delimiters.LeftParentheses), ExpectException("Missing left parentheses.", "("))
    val params = mutableListOf<Parameter>()
    var isComma = false

    for (token in lexer) {
        val type = token.type
        isComma = when {
            type is Tokens.Delimiter && type.type == Delimiters.RightParentheses -> return params
            type is Tokens.Punctuation && type.punctuation == Punctuations.Comma && !isComma -> true
            else -> {
                val name = expect(Tokens.Identifier, ExpectException("Missing parameter name.", "Identifier"))
                val colon = expect(Tokens.Punctuation(Punctuations.Colon), ExpectException("Missing Colon.", ":"))
                val parameterType = expect(Tokens.Identifier, ExpectException("Missing parameter type.", "Identifier"))

                params.add(Parameter(name, colon, parameterType))
                false
            }
        }
    }

    throw ExpectException("Unclosed delimiter", "(")
}
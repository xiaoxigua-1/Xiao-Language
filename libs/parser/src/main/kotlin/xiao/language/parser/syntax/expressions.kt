package xiao.language.parser.syntax

import xiao.language.parser.Parser
import xiao.language.parser.expect
import xiao.language.utilities.Token
import xiao.language.utilities.ast.Expressions
import xiao.language.utilities.exceptions.Exceptions
import xiao.language.utilities.tokens.Punctuations
import xiao.language.utilities.tokens.Tokens
import java.util.concurrent.ExecutionException

fun Parser.expressions(): Expressions {
    for (token in lexer) {
        val type = token.type

        return when {
            type is Tokens.Identifier && lexer.peek().type == Tokens.Punctuation(Punctuations.PathSep) -> path()
            type is Tokens.Literal || type is Tokens.Identifier && lexer.peek().type == Tokens.Punctuation(Punctuations.Colon) -> sub()
            type is Tokens.Identifier -> identifier()
            else -> throw Exceptions.ExpectException("Not expression", token.span)
        }
    }

    throw Exceptions.EOFException("Expect expression")
}

fun Parser.path(): Expressions.Path {
    val main = Expressions.Identifier(lexer.next())
    expect(Tokens.Punctuation(Punctuations.PathSep), Exceptions.ExpectException("::", main.name.span))

    return Expressions.Path(main, expressions())
}

fun Parser.sub(): Expressions.Sub {
    val main = Expressions.Identifier(lexer.next())
    expect(Tokens.Punctuation(Punctuations.Colon), Exceptions.ExpectException("::", main.name.span))

    return Expressions.Sub(main, expressions())
}

fun Parser.identifier(): Expressions {
    val token = expect(Tokens.Identifier, Exceptions.EOFException("Expected identifier."))
    return Expressions.Identifier(token)
}

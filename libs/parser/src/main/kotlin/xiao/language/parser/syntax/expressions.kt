package xiao.language.parser.syntax

import xiao.language.parser.Parser
import xiao.language.parser.expect
import xiao.language.parser.statements
import xiao.language.utilities.Span
import xiao.language.utilities.Token
import xiao.language.utilities.ast.Expressions
import xiao.language.utilities.ast.Statement
import xiao.language.utilities.exceptions.Exceptions
import xiao.language.utilities.tokens.Delimiters
import xiao.language.utilities.tokens.Punctuations
import xiao.language.utilities.tokens.Tokens

fun Parser.expressions(): Expressions {
    for (token in lexer) {
        val type = token.type

        return when {
            type is Tokens.Whitespace -> continue
            type is Tokens.Identifier && lexer.peek().type == Tokens.Punctuation(Punctuations.PathSep) -> path(token)
            type is Tokens.Literal || type is Tokens.Identifier && lexer.peek().type == Tokens.Punctuation(Punctuations.Colon) -> sub(
                token
            )

            type is Tokens.Identifier -> Expressions.Identifier(token, token.span)
            type == Tokens.Delimiter(Delimiters.LeftCurlyBraces) -> block(token)
            else -> throw Exceptions.ExpectException("Not expression ${token.type}", token.span)
        }
    }

    throw Exceptions.EOFException("Expect expression")
}

fun Parser.path(token: Token): Expressions.Path {
    val main = Expressions.Identifier(token, token.span)
    expect(Tokens.Punctuation(Punctuations.PathSep), Exceptions.ExpectException("::", main.name.span))
    val expression = expressions()

    return Expressions.Path(main, expression, Span(token.span.start, expression.span.end))
}

fun Parser.sub(token: Token): Expressions.Sub {
    val main = Expressions.Identifier(token, token.span)
    expect(Tokens.Punctuation(Punctuations.Colon), Exceptions.ExpectException("::", main.name.span))
    val expression = expressions()

    return Expressions.Sub(main, expression, Span(token.span.start, expression.span.end))
}

fun Parser.block(left: Token): Expressions.Block {
    val statements = mutableListOf<Statement>()
    for (token in lexer) {
        when (token.type) {
            is Tokens.Whitespace -> continue
            Tokens.Delimiter(Delimiters.RightCurlyBraces) -> return Expressions.Block(
                left,
                listOf(),
                token,
                Span(left.span.start, token.span.end)
            )

            else -> try {
                statements.add(statements())
            } catch (e: Exception) {
                break
            }
        }
    }

    throw Exceptions.UnterminatedException("Unterminated block", left.span)
}
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
            type is Tokens.Whitespace || type is Tokens.NewLine -> continue
            type is Tokens.Identifier && lexer.peek().type == Tokens.Punctuation(Punctuations.PathSep) -> path(token)
            type is Tokens.Identifier && lexer.peek().type == Tokens.Delimiter(Delimiters.LeftParentheses) -> call(token)
            type is Tokens.Literal && type is Tokens.Identifier && lexer.peek().type == Tokens.Punctuation(Punctuations.Colon) -> sub(
                token
            )

            type is Tokens.Literal -> Expressions.String(token)
            type is Tokens.Identifier -> Expressions.Identifier(token, token.span)
            type == Tokens.Punctuation(Punctuations.Eq) -> eqValue(token)
            type == Tokens.Delimiter(Delimiters.LeftCurlyBraces) -> block(token)
            else -> throw Exceptions.ExpectException("Not expression ${token.type}", token.span)
        }
    }

    throw Exceptions.EOFException("Expect expression")
}

/**
 * parse call expression function
 */
fun Parser.call(name: Token): Expressions.Call {
    val left =
        expect(Tokens.Delimiter(Delimiters.LeftParentheses), Exceptions.ExpectException("Exception `(`", name.span))
    var comma: Token? = null
    val args = mutableListOf<Expressions>()

    while (lexer.hasNext()) {
        val token = lexer.peek()

        when (token.type) {
            Tokens.Delimiter(Delimiters.RightParentheses) -> return Expressions.Call(
                name,
                listOf(),
                Span(name.span.start, lexer.next().span.end)
            )

            Tokens.Punctuation(Punctuations.Comma) -> if (comma == null) comma =
                lexer.next() else throw Exceptions.ExpectException("Expect expression, found `,`", token.span)

            else -> {
                comma = null
                args.add(expressions())
            }
        }
    }

    throw Exceptions.UnterminatedException("Unterminated delimiter", left.span)
}

/**
 * parse path expression function
 */
fun Parser.path(token: Token): Expressions.Path {
    val main = Expressions.Identifier(token, token.span)
    expect(Tokens.Punctuation(Punctuations.PathSep), Exceptions.ExpectException("Exception `::`", main.name.span))
    val expression = expressions()

    return Expressions.Path(main, expression)
}

/**
 * parse sub expression function
 */
fun Parser.sub(token: Token): Expressions.Sub {
    val main = Expressions.Identifier(token, token.span)
    expect(Tokens.Punctuation(Punctuations.Colon), Exceptions.ExpectException("Exception `.`", main.name.span))
    val expression = expressions()

    return Expressions.Sub(main, expression)
}

/**
 * parse block expression function
 */
fun Parser.block(left: Token): Expressions.Block {
    val statements = mutableListOf<Statement>()
    while (lexer.hasNext()) {
        val token = lexer.peek()
        when (token.type) {
            Tokens.Whitespace, Tokens.NewLine -> {
                lexer.next()
                continue
            }

            Tokens.Delimiter(Delimiters.RightCurlyBraces) -> return Expressions.Block(
                left,
                statements,
                lexer.next()
            )

            else -> statements.add(statements() ?: break)
        }
    }

    throw Exceptions.UnterminatedException("Unterminated block", left.span)
}

fun Parser.eqValue(eq: Token): Expressions.EqValue {
    return Expressions.EqValue(eq, expressions())
}

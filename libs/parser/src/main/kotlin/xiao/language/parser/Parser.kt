package xiao.language.parser

import xiao.language.lexer.Lexer
import xiao.language.parser.syntax.expressions
import xiao.language.parser.syntax.statements.function
import xiao.language.utilities.Token
import xiao.language.utilities.ast.Statement
import xiao.language.utilities.ast.Visibility
import xiao.language.utilities.exceptions.Exceptions
import xiao.language.utilities.tokens.Keywords
import xiao.language.utilities.tokens.Tokens

data class Parser(val lexer: Lexer) : Iterator<Statement> {
    private var isEOF = false

    override fun hasNext(): Boolean = !isEOF

    override fun next(): Statement {
        TODO("next statement")
    }
}

fun Parser.statements(): Statement {
    if (lexer.hasNext()) {
        val token = lexer.next()
        return when (val type = token.type) {
            is Tokens.Keyword -> keyword(type.type, token)
            else -> Statement.Expression(expressions())
        }
    } else throw Exceptions.EOFException("EOF")
}

internal fun Parser.expect(type: Tokens, exception: Exceptions): Token {
    for (token in lexer) {
        println(token.type)
        when (token.type) {
            Tokens.Whitespace -> continue
            type -> return token
            else -> throw Exceptions.ExpectException(exception.message!!, exception.span ?: token.span)
        }
    }

    throw exception
}

private fun Parser.keyword(kwdType: Keywords, kwd: Token): Statement {
    return when (kwdType) {
        Keywords.Fn -> function(Visibility.Private, kwd)
        else -> throw Exceptions.EOFException("")
    }
}
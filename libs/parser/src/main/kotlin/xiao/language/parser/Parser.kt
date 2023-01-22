package xiao.language.parser

import xiao.language.lexer.Lexer
import xiao.language.parser.syntax.*
import xiao.language.utilities.Token
import xiao.language.utilities.ast.Statement
import xiao.language.utilities.ast.Visibility
import xiao.language.utilities.exceptions.EOFException
import xiao.language.utilities.tokens.Keywords
import xiao.language.utilities.tokens.Tokens
import java.lang.Exception

data class Parser(val lexer: Lexer) : Iterator<Statement> {
    private var isEOF = false

    override fun hasNext(): Boolean = !isEOF

    override fun next(): Statement {
        TODO("next statement")
    }
}

fun Parser.nextStatement() {
    if (lexer.hasNext()) {
        val token = lexer.next()
        when (val type = token.type) {
            is Tokens.Keyword -> keyword(type.type, token)
            else -> {}
        }
    } else {
    }
}

internal fun Parser.expect(type: Tokens, exception: Exception): Token {
    for (token in lexer) {
        return if (token.type == type) token
        else break
    }
    throw exception
}

private fun Parser.keyword(kwdType: Keywords, kwd: Token) {
    when (kwdType) {
        Keywords.Fn -> function(Visibility.Private, kwd)
        else -> {}
    }
}
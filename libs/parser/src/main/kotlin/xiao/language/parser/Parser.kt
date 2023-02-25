package xiao.language.parser

import xiao.language.lexer.Lexer
import xiao.language.parser.syntax.expressions
import xiao.language.parser.syntax.statements.function
import xiao.language.parser.syntax.statements.public
import xiao.language.parser.syntax.statements.returnStatement
import xiao.language.utilities.Token
import xiao.language.utilities.ast.Statement
import xiao.language.utilities.exceptions.Exceptions
import xiao.language.utilities.tokens.Keywords
import xiao.language.utilities.tokens.Punctuations
import xiao.language.utilities.tokens.Tokens

data class Parser(val lexer: Lexer) : Iterator<Statement> {
    override fun hasNext(): Boolean = lexer.peek().type !is Tokens.EOF

    override fun next(): Statement {
        return statements()!!
    }
}

fun Parser.statements(): Statement? {
    return if (lexer.hasNext()) {
        while (lexer.hasNext()) {
            val token = lexer.peek()
            when (token.type) {
                is Tokens.Whitespace, is Tokens.NewLine -> lexer.next()
                else -> break
            }
        }

        when (val type = lexer.peek().type) {
            is Tokens.Keyword -> keyword(type.type, lexer.next())
            else -> {
                val statement = Statement.Expression(expressions())
                expect(Tokens.Punctuation(Punctuations.Semi), Exceptions.ExpectException("Missing `;`", statement.span))
                statement
            }
        }
    } else null
}

internal fun Parser.expect(type: Tokens, exception: Exceptions): Token {
    for (token in lexer) {
        when (token.type) {
            Tokens.Whitespace, Tokens.NewLine -> continue
            type -> return token
            else -> throw Exceptions.ExpectException(
                "${exception.message!!}, found `${token.value}`",
                exception.span ?: token.span
            )
        }
    }

    throw exception
}

private fun Parser.keyword(kwdType: Keywords, kwd: Token): Statement {
    return when (kwdType) {
        Keywords.Fn -> function(kwd)
        Keywords.Return -> returnStatement(kwd)
        Keywords.Pub -> public(kwd)
        else -> throw Exceptions.EOFException("")
    }
}
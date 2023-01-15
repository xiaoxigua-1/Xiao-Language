/*
 * This Library is xiao language lexer
 */
package xiao.language.lexer

import xiao.language.utilities.isAsciiSymbol

class Lexer(
    val fileStream: FileStream
): Iterator<Token> {
    private var isEOF = false

    override fun hasNext(): Boolean = !isEOF

    override fun next(): Token {
        val token = nextToken()

        return token ?: run {
            isEOF = true
            Token(Tokens.EOF, ' ', Span(fileStream.getIndex()))
        }
    }
}

fun Lexer.nextToken(): Token? {
    return if (fileStream.hasNext()) {
        val c = fileStream.next()

        when {
            c.isWhitespace() -> whitespace(fileStream.getIndex())
            c == '"' || c == '\'' -> stringOrChar(fileStream.getIndex(), c)
            c.isAsciiSymbol() -> Token(Tokens.Symbol, c, Span(fileStream.getIndex()))
            else -> Token(Tokens.Unknown, c, Span(fileStream.getIndex()))
        }
    } else {
        null
    }
}

private fun Lexer.whitespace(start: Int): Token {
    var value = fileStream.current.toString()

    do {
        val c = fileStream.peek()
        when {
            c?.isWhitespace() ?: false -> value += fileStream.next()
            else -> break
        }
    } while(fileStream.hasNext())

    return Token(Tokens.Whitespace, value, Span(start, fileStream.getIndex()))
}

private fun Lexer.stringOrChar(start: Int, startChar: Char): Token {
    var value = ""

    for (c in fileStream) {
        when(c) {
            startChar -> {
                break
            }
            else -> value += c
        }
    }

    return Token(Tokens.Literal, value, Span(start, fileStream.getIndex()))
}
package xiaoLanguage.lexer

import xiaoLanguage.exception.SyntaxError
import xiaoLanguage.tokens.Position
import xiaoLanguage.tokens.Token
import xiaoLanguage.tokens.TokenType
import xiaoLanguage.tokens.Tokens
import xiaoLanguage.util.StringStream

class Lexer(private val stringStream: StringStream) {
    private var lineNumber = 0
    private var index = 0

    fun lex(): MutableList<Token> {
        val tokens = mutableListOf<Token>()
        var str = ""
        var start = 0

        while (!stringStream.isEOF) {
            when (stringStream.currently) {
                in Tokens.values().map { it.token }, " ", "\n", "\r" -> {
                    if (str.isNotEmpty()) {
                        tokens += Token(
                            str,
                            Position(lineNumber, start, start + str.length - 1),
                            TokenType.IDENTIFIER_TOKEN
                        )

                        str = ""
                    }
                }
            }

            when (stringStream.currently) {
                "\n", "\r" -> {
                    lineNumber++
                    index = -1
                }
                " ", "\t", "\b" -> {}
                Tokens.LEFT_CURLY_BRACKETS_TOKEN.token -> tokens += Token(
                    stringStream.currently,
                    Position(lineNumber, index),
                    TokenType.LEFT_CURLY_BRACKETS_TOKEN
                )
                Tokens.LEFT_PARENTHESES_TOKEN.token -> tokens += Token(
                    stringStream.currently,
                    Position(lineNumber, index),
                    TokenType.LEFT_PARENTHESES_TOKEN
                )
                Tokens.LEFT_SQUARE_BRACKETS_TOKEN.token -> tokens += Token(
                    stringStream.currently,
                    Position(lineNumber, index),
                    TokenType.LEFT_SQUARE_BRACKETS_TOKEN
                )
                Tokens.RIGHT_CURLY_BRACKETS_TOKEN.token -> tokens += Token(
                    stringStream.currently,
                    Position(lineNumber, index),
                    TokenType.RIGHT_CURLY_BRACKETS_TOKEN
                )
                Tokens.RIGHT_PARENTHESES_TOKEN.token -> tokens += Token(
                    stringStream.currently,
                    Position(lineNumber, index),
                    TokenType.RIGHT_PARENTHESES_TOKEN
                )
                Tokens.RIGHT_SQUARE_BRACKETS_TOKEN.token -> tokens += Token(
                    stringStream.currently,
                    Position(lineNumber, index),
                    TokenType.RIGHT_SQUARE_BRACKETS_TOKEN
                )
                Tokens.DOUBLE_QUOTES_TOKEN.token, Tokens.SINGLE_QUOTES_TOKEN.token -> tokens += string()
//                in ("0".."9"), ".", "-" -> tokens += number()

                else -> {
                    if (str.isEmpty()) start = index
                    str += stringStream.currently
                }
            }

            stringStream.nextChar()
            index++
        }

        return tokens
    }

    private fun string(): Token {
        var str = ""
        val start = index

        stringStream.nextChar()

        while (!stringStream.isEOF) {
            when (stringStream.currently) {
                Tokens.BACKSLASH_TOKEN.token -> {
                    stringStream.nextChar()
                    when (stringStream.currently) {
                        "n" -> str += '\n'
                        "r" -> str += '\r'
                        "b" -> str += '\b'
                        "t" -> str += '\t'
                        else -> str += stringStream.currently
                    }
                }
                Tokens.DOUBLE_QUOTES_TOKEN.token, Tokens.SINGLE_QUOTES_TOKEN.token -> {
                    return Token(
                        str,
                        Position(lineNumber, start, ++index),
                        TokenType.STRING_LITERAL_TOKEN
                    )
                }
                "\n", "\r" -> {
                    index = 0
                    break
                }
                else -> str += stringStream.currently
            }
            index++
            stringStream.nextChar()
        }

        throw SyntaxError("EOL while scanning string literal")
    }
//
//    fun number(): Token {
//    }
}
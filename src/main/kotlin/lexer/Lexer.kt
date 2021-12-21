package xiaoLanguage.lexer

import xiaoLanguage.exception.SyntaxError
import xiaoLanguage.tokens.Position
import xiaoLanguage.tokens.Token
import xiaoLanguage.tokens.TokenType
import xiaoLanguage.tokens.Tokens
import xiaoLanguage.util.StringStream

class Lexer(private val stringStream: StringStream) {
    private var index = 0
    var lineNumber = 0
    var exceptionIndex = 0

    fun lex(): MutableList<Token> {
        val tokens = mutableListOf<Token>()
        var str = ""
        var start = 0

        while (!stringStream.isEOF) {
            when (stringStream.currently) {
                in Tokens.values().map { it.token }, " ", "\n", "\r" -> {
                    if (str.isNotEmpty()) {
                        tokens += Token(
                            str, Position(lineNumber, start, start + str.length - 1), TokenType.IDENTIFIER_TOKEN
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
                    stringStream.currently, Position(lineNumber, index), TokenType.LEFT_CURLY_BRACKETS_TOKEN
                )
                Tokens.LEFT_PARENTHESES_TOKEN.token -> tokens += Token(
                    stringStream.currently, Position(lineNumber, index), TokenType.LEFT_PARENTHESES_TOKEN
                )
                Tokens.LEFT_SQUARE_BRACKETS_TOKEN.token -> tokens += Token(
                    stringStream.currently, Position(lineNumber, index), TokenType.LEFT_SQUARE_BRACKETS_TOKEN
                )
                Tokens.RIGHT_CURLY_BRACKETS_TOKEN.token -> tokens += Token(
                    stringStream.currently, Position(lineNumber, index), TokenType.RIGHT_CURLY_BRACKETS_TOKEN
                )
                Tokens.RIGHT_PARENTHESES_TOKEN.token -> tokens += Token(
                    stringStream.currently, Position(lineNumber, index), TokenType.RIGHT_PARENTHESES_TOKEN
                )
                Tokens.RIGHT_SQUARE_BRACKETS_TOKEN.token -> tokens += Token(
                    stringStream.currently, Position(lineNumber, index), TokenType.RIGHT_SQUARE_BRACKETS_TOKEN
                )
                Tokens.MULTIPLY_TOKEN.token -> tokens += Token(
                    stringStream.currently, Position(lineNumber, index), TokenType.MULTIPLY_TOKEN
                )
                Tokens.PLUS_TOKEN.token -> tokens += Token(
                    stringStream.currently, Position(lineNumber, index), TokenType.PLUS_TOKEN
                )
                Tokens.MINUS_TOKEN.token -> tokens += Token(
                    stringStream.currently, Position(lineNumber, index), TokenType.MINUS_TOKEN
                )
                Tokens.COLON_TOKEN.token -> tokens += Token(
                    stringStream.currently, Position(lineNumber, index), TokenType.COLON_TOKEN
                )
                Tokens.COMMA_TOKEN.token -> tokens += Token(
                    stringStream.currently, Position(lineNumber, index), TokenType.COMMA_TOKEN
                )
                Tokens.EQUAL_TOKEN.token -> tokens += Token(
                    stringStream.currently, Position(lineNumber, index), TokenType.EQUAL_TOKEN
                )
                Tokens.DOUBLE_QUOTES_TOKEN.token, Tokens.SINGLE_QUOTES_TOKEN.token -> tokens += string()
                Tokens.SLASH_TOKEN.token -> {
                    val token = slash()
                    if (token != null) tokens += token
                }
                in ("0".."9"), Tokens.DOT_TOKEN.token -> tokens += number()

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
        exceptionIndex = index

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
                        str, Position(lineNumber, start, ++index), TokenType.STRING_LITERAL_TOKEN
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

    private fun number(): Token {
        var str = ""
        val start = index
        exceptionIndex = index
        var isFloat = false

        // float .3
        when (stringStream.currently) {
            Tokens.DOT_TOKEN.token -> {
                stringStream.nextChar()
                if (stringStream.currently in ("0".."9")) {
                    str = "."
                    isFloat = true
                    index++
                } else {
                    stringStream.backChar()
                    return Token(
                        stringStream.currently,
                        Position(lineNumber, start, start),
                        TokenType.DOT_TOKEN
                    )
                }
            }
        }

        // float and int 10.3 or 20
        while (!stringStream.isEOF) {
            when (stringStream.currently) {
                in ("0".."9"), Tokens.DOT_TOKEN.token -> {
                    if (stringStream.currently == Tokens.DOT_TOKEN.token) isFloat = true
                    str += stringStream.currently
                }
                in Tokens.values().map { it.token }, "\n", "\r" -> {
                    stringStream.backChar()
                    index--
                    return Token(
                        str,
                        Position(lineNumber, start, index),
                        if (isFloat) TokenType.FLOAT_LITERAL_TOKEN else TokenType.INTEGER_LITERAL_TOKEN
                    )
                }
                else -> break
            }

            index++
            stringStream.nextChar()
        }

        throw SyntaxError("invalid syntax")
    }

    private fun slash(): Token? {
        var str = "/"

        stringStream.nextChar()

        when (stringStream.currently) {
            Tokens.SLASH_TOKEN.token -> {
                while (!stringStream.isEOF) {
                    when (stringStream.currently) {
                        Tokens.SLASH_TOKEN.token -> str += stringStream.currently
                        "\r", "\n" -> {
                            index = 0
                            lineNumber++
                            stringStream.nextChar()
                            return null
                        }
                    }

                    index++
                    stringStream.nextChar()
                }
            }
            Tokens.MULTIPLY_TOKEN.token -> {
                while (!stringStream.isEOF) {
                    when (stringStream.currently) {
                        Tokens.MULTIPLY_TOKEN.token -> {
                            stringStream.nextChar()
                            if (stringStream.currently == Tokens.SLASH_TOKEN.token) {
                                return null
                            } else stringStream.backChar()
                        }
                        "\r", "\n" -> {
                            index = 0
                            lineNumber++
                        }
                    }

                    index++
                    stringStream.nextChar()
                }
            }
            else ->{
                stringStream.backChar()
                return Token(
                    str,
                    Position(lineNumber, index),
                    TokenType.SLASH_TOKEN
                )
            }
        }

        throw SyntaxError("invalid syntax")
    }
}
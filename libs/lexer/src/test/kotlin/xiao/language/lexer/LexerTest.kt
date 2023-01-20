package xiao.language.lexer

import xiao.language.utilities.Tokens
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class LexerTest {
    private val identTestFile = FileStreamTest::class.java.getResource("/idents.xiao")!!
    private val literalTestFile = FileStreamTest::class.java.getResource("/literals.xiao")!!
    private val numbersTestFile = FileStreamTest::class.java.getResource("/numbers.xiao")!!
    private val delimitersTestFile = FileStreamTest::class.java.getResource("/delimiters.xiao")!!
    private val punctuationTestFile = FileStreamTest::class.java.getResource("/punctuations.xiao")!!

    @Test
    fun lexerIdentTest() {
        val idents = listOf(
            "__test__", "__test", "test", "test__", "__哈摟__", "__哈摟", "哈摟", "哈摟__"
        ).iterator()
        val lexer = Lexer(FileStream(identTestFile))

        lexerTest(lexer, Tokens.Identifier, idents)
    }

    @Test
    fun lexerLiteralTest() {
        val literals = listOf(
            """"abc_ def 嗨 $#!"""", """'abc'"""
        ).iterator()
        val lexer = Lexer(FileStream(literalTestFile))

        lexerTest(lexer, Tokens.Literal, literals)
    }

    @Test
    fun lexerNumberTest() {
        val numbers = listOf(
            "091231231231", "0x1234567890abcdef", "0b10101100", "0o1234567"
        ).iterator()
        val lexer = Lexer(FileStream(numbersTestFile))

        lexerTest(lexer, Tokens.Number, numbers)
    }

    @Test
    fun lexerDelimiterTest() {
        val delimiters = listOf(
            "{", "}", "[", "]", "(", ")"
        ).iterator()
        val lexer = Lexer(FileStream(delimitersTestFile))

        lexerTest(lexer, Tokens.Delimiters, delimiters)
    }

    @Test
    fun lexerPunctuationTest() {
        val punctuations = listOf(
            "::",
            ":",
            "+",
            "-",
            "/",
            "%",
            "||",
            "|",
            "&",
            "&&",
            "!",
        ).iterator()
        val lexer = Lexer(FileStream(punctuationTestFile))

        lexerTest(lexer, Tokens.Punctuation, punctuations)
    }

    private fun lexerTest(lexer: Lexer, expectedType: Tokens, expected: Iterator<String>) {
        for (token in lexer) {
            if (token.type == expectedType) {
                assertEquals(expected.next(), token.value)
            }
        }
    }
}
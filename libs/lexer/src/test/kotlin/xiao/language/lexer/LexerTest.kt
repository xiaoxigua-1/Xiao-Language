package xiao.language.lexer

import xiao.language.utilities.Tokens
import kotlin.test.Test
import kotlin.test.assertEquals

class LexerTest {
    private val identTestFile = FileStreamTest::class.java.getResource("/idents.xiao")

    @Test
    fun lexerIdentTest() {
        val idents = listOf(
            "__test__",
            "__test",
            "test",
            "test__",
            "__哈摟__",
            "__哈摟",
            "哈摟",
            "哈摟__"
        ).iterator()
        val lexer = Lexer(FileStream(identTestFile))

        for (token in lexer) {
            if (token.type == Tokens.Identifier) {
                assertEquals(idents.next(), token.value)
            }
        }
    }
}
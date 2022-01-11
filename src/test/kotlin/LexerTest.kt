import xiaoLanguage.lexer.Lexer
import xiaoLanguage.tokens.TokenType
import xiaoLanguage.util.StringStream
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class LexerTest {
    @Test
    fun lexTest() {
        val file = File(this::class.java.getResource("/lexerTestData/test.xiao")!!.path)
        val fileLines = file.readLines()
        val stringStream = StringStream(file)
        val lex = Lexer(stringStream).lex()
        val expectedData = listOf(
            TokenType.LEFT_CURLY_BRACKETS_TOKEN,
            TokenType.RIGHT_CURLY_BRACKETS_TOKEN,
            TokenType.LEFT_PARENTHESES_TOKEN,
            TokenType.RIGHT_PARENTHESES_TOKEN,
            TokenType.LEFT_SQUARE_BRACKETS_TOKEN,
            TokenType.RIGHT_SQUARE_BRACKETS_TOKEN,
            TokenType.STRING_LITERAL_TOKEN,
            TokenType.STRING_LITERAL_TOKEN,
            TokenType.ARROW_TOKEN,
            TokenType.MINUS_TOKEN,
            TokenType.PLUS_TOKEN,
            TokenType.SLASH_TOKEN,
            TokenType.COLON_TOKEN,
            TokenType.COMMA_TOKEN,
            TokenType.MORE_TOKEN,
            TokenType.LESS_TOKEN,
            TokenType.EQUAL_TOKEN,
            TokenType.DOUBLE_EQUAL_TOKEN,
            TokenType.FLOAT_LITERAL_TOKEN,
            TokenType.FLOAT_LITERAL_TOKEN,
            TokenType.INTEGER_LITERAL_TOKEN,
            TokenType.DOT_TOKEN
        )

        lex.mapIndexed { index, token ->
            assertEquals(expectedData[index], token.tokenType)
            assertEquals(index, token.position.lineNumber)
            if (token.tokenType != TokenType.STRING_LITERAL_TOKEN) {
                assertEquals(
                    token.literal,
                    fileLines[token.position.lineNumber].slice(token.position.start..token.position.end)
                )
            }
        }
    }
}
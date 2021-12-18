import xiaoLanguage.lexer.Lexer
import xiaoLanguage.util.StringStream
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class LexerTest {
    @Test
    fun lexTest() {
        val file = File(this::class.java.getResource("/test.xiao").path)
        val stringStream = StringStream(file)
        Lexer(stringStream).lex()
    }
}
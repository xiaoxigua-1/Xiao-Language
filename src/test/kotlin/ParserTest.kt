import xiaoLanguage.lexer.Lexer
import xiaoLanguage.parser.Parser
import xiaoLanguage.util.StringStream
import java.io.File
import kotlin.test.Test

class ParserTest {
    @Test
    fun parserTest() {
        val file = File(this::class.java.getResource("/test.xiao").path)
        val stringStream = StringStream(file)
        val lex = Lexer(stringStream)
        val parser = Parser(lex, file)
        parser.parser()
    }
}
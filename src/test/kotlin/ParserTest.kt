import xiaoLanguage.ast.Import
import xiaoLanguage.lexer.Lexer
import xiaoLanguage.parser.Parser
import xiaoLanguage.util.StringStream
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {
    @Test
    fun parserTest() {
        val file = File(this::class.java.getResource("/test.xiao").path)
        val stringStream = StringStream(file)
        val lex = Lexer(stringStream)
        val parser = Parser(lex, file)
        parser.parser()
    }

    @Test
    fun parserImportTest() {
        val assertData = listOf("xiao/Math", "test2")
        val file = File(this::class.java.getResource("/import/import.xiao").path)
        val stringStream = StringStream(file)
        val lex = Lexer(stringStream)
        val parser = Parser(lex, file)
        val (ast, report) = parser.parser()

        for (report in report) {
            report.printReport(file.readLines(), "/import/import.xiao")
        }

        ast.mapIndexed { index ,node ->
            if (node is Import) {
                val path = node.path.joinToString("/") { it.literal }

                assertEquals(assertData[index] , path)
            }
        }
    }
}
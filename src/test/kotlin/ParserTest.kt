import xiaoLanguage.ast.Function
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
        val file = File(this::class.java.getResource("/test.xiao")!!.path)
        val stringStream = StringStream(file)
        val lex = Lexer(stringStream)
        val parser = Parser(lex, file)
        parser.parser()
    }

    @Test
    fun parserImportTest() {
        val assertData = listOf("xiao/Math", "test2", "xiao/Math/plus")
        val file = File(this::class.java.getResource("/import/import.xiao")!!.path)
        val stringStream = StringStream(file)
        val lex = Lexer(stringStream)
        val parser = Parser(lex, file)
        val (ast, reports) = parser.parser()

        for (report in reports) {
            report.printReport(file.readLines(), "/import/import.xiao")
        }

        ast.mapIndexed { index ,node ->
            if (node is Import) {
                val path = node.path.joinToString("/") { it.literal }

                assertEquals(assertData[index] , path)
            }
        }
    }

    @Test
    fun parserFunctionTest() {
        val assertDataName = listOf("main", "test23", "test33", "test34", "test66")
        val assertDataArgsName = listOf(null, null, listOf("name"), listOf("name", "info"), null)
        val assertDataArgsType = listOf(null, null, listOf("Str"), listOf("Str", "Int"), null)
        val assertDataReturnType = listOf(null, null, null, null, "Str")
        val file = File(this::class.java.getResource("/functions/functions.xiao")!!.path)
        val stringStream = StringStream(file)
        val lex = Lexer(stringStream)
        val parser = Parser(lex, file)
        val (ast, reports) = parser.parser()

        for (report in reports) {
            report.printReport(file.readLines(), "/functions/functions.xiao")
        }

        ast.mapIndexed { index ,node ->
            if (node is Function) {
                val name = node.functionName.literal

                assertEquals(assertDataName[index] , name)
                node.parameters.mapIndexed { index2, parameter ->
                    assertEquals(assertDataArgsName[index]?.get(index2), parameter.name.literal)
                    assertEquals(assertDataArgsType[index]?.get(index2), parameter.type.typeTokens.literal)
                }

                assertEquals(assertDataReturnType[index], node.returnType?.typeTokens?.literal)
            }
        }
    }
}
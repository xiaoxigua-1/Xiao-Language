import xiaoLanguage.ast.ASTNode
import xiaoLanguage.ast.Class
import xiaoLanguage.ast.Function
import xiaoLanguage.ast.Import
import xiaoLanguage.lexer.Lexer
import xiaoLanguage.parser.Parser
import xiaoLanguage.util.StringStream
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {
    /*
        parse to Abstract Syntax Tree
    */
    private fun parserTest(filePath: String): MutableList<ASTNode> {
        val file = File(this::class.java.getResource(filePath)!!.path)
        val stringStream = StringStream(file)
        val lex = Lexer(stringStream)
        val parser = Parser(lex, file)
        val (ast, reports) = parser.parser()

        for (report in reports) {
            report.printReport(file.readLines(), filePath)
        }

        return ast
    }

    /*
        The correctness of parse import
    */
    @Test
    fun parserImportTest() {
        val expectedData = listOf("xiao/Math", "test2", "xiao/Math/plus")
        val ast = parserTest("/import/import.xiao")

        ast.mapIndexed { index, node ->
            if (node is Import) {
                val path = node.path.joinToString("/") { it.literal }

                assertEquals(expectedData[index], path)
            }
        }
    }

    /*
        The correctness of parse function
    */
    @Test
    fun parserFunctionTest() {
        val expectedDataName = listOf("main", "test23", "test33", "test34", "test66")
        val expectedDataArgsName = listOf(null, null, listOf("name"), listOf("name", "info"), null)
        val expectedDataArgsType = listOf(null, null, listOf("Str"), listOf("Str", "Int"), null)
        val expectedDataReturnType = listOf(null, null, null, null, "Str")
        val ast = parserTest("/functions/function.xiao")

        ast.mapIndexed { index, node ->
            if (node is Function) {
                val name = node.functionName.literal

                assertEquals(expectedDataName[index], name)

                node.parameters.mapIndexed { index2, parameter ->
                    assertEquals(expectedDataArgsName[index]?.get(index2), parameter.name.literal)
                    assertEquals(expectedDataArgsType[index]?.get(index2), parameter.type.typeTokens.literal)
                }

                assertEquals(expectedDataReturnType[index], node.returnType?.typeTokens?.literal)
            }
        }
    }

    /*
        The correctness of parse class
     */
    @Test
    fun parserClassTest() {
        val ast = parserTest("/classes/class.xiao")
        val expectedDataName = listOf("A", "B12", "ABC")

        ast.mapIndexed { index, astNode ->
            if (astNode is Class) {
                assertEquals(expectedDataName[index], astNode.className.literal)
            }
        }
    }
}
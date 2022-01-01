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
    data class ExpectedClassData(val name: String)

    data class ExpectedFunctionData(val name: String, val parameter: List<String>? = null, val parameterType: List<String>? = null, val returnType: String? = null)
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
        val expectedData = listOf(
            ExpectedFunctionData("main"),
            ExpectedFunctionData("test23"),
            ExpectedFunctionData("test33", listOf("name"), listOf("Str")),
            ExpectedFunctionData("test34", listOf("name", "info"), listOf("Str", "Int")),
            ExpectedFunctionData("test66", null, null, "Str")
        )
        val ast = parserTest("/functions/function.xiao")

        ast.mapIndexed { index, node ->
            if (node is Function) {
                val name = node.functionName.literal

                assertEquals(expectedData[index].name, name)

                node.parameters.mapIndexed { index2, parameter ->
                    assertEquals(expectedData[index].parameter?.get(index2), parameter.name.literal)
                    assertEquals(expectedData[index].parameterType?.get(index2), parameter.type.typeTokens.literal)
                }

                assertEquals(expectedData[index].returnType, node.returnType?.typeTokens?.literal)
            }
        }
    }

    /*
        The correctness of parse class
     */
    @Test
    fun parserClassTest() {
        val ast = parserTest("/classes/class.xiao")
        val expectedData = listOf(
            ExpectedClassData("A"),
            ExpectedClassData("B12"),
            ExpectedClassData("ABC")
        )

        ast.mapIndexed { index, astNode ->
            if (astNode is Class) {
                assertEquals(expectedData[index].name, astNode.className.literal)
            }
        }
    }
}
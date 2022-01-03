import xiaoLanguage.ast.*
import xiaoLanguage.ast.Function
import xiaoLanguage.lexer.Lexer
import xiaoLanguage.parser.Parser
import xiaoLanguage.util.StringStream
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {
    data class ExpectedClassData(
        val name: String,
        val functions: List<String>? = null,
        val functionParameters: List<String>? = null,
        val functionParameterType: List<String>? = null
    )

    data class ExpectedFunctionData(
        val name: String,
        val parameter: List<String>? = null,
        val parameterType: List<String>? = null,
        val returnType: String? = null
    )

    data class ExpectedVariableData(
        val name: String,
        val value: String,
        val type: String? = null
    )

    /**
     * parse to Abstract Syntax Tree
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

    /**
     * The correctness of parse import
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

    /**
     * The correctness of parse function
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

    /**
     * The correctness of parse class
     */
    @Test
    fun parserClassTest() {
        val ast = parserTest("/classes/class.xiao")
        val expectedData = listOf(
            ExpectedClassData("A"),
            ExpectedClassData("B12"),
            ExpectedClassData("ABC", listOf("test")),
            ExpectedClassData("AAC", listOf("test", "test2")),
            ExpectedClassData("AAA", listOf("test"), listOf("name"), listOf("Str"))
        )

        ast.mapIndexed { index, astNode ->
            if (astNode is Class) {
                assertEquals(expectedData[index].name, astNode.className.literal)

                astNode.functions.mapIndexed { index2, function ->
                    assertEquals(expectedData[index].functions?.get(index2), function.functionName.literal)
                    function.parameters.mapIndexed { index3, parameter ->
                        assertEquals(expectedData[index].functionParameters?.get(index3), parameter.name.literal)
                        assertEquals(
                            expectedData[index].functionParameterType?.get(index3),
                            parameter.type.typeTokens.literal
                        )
                    }
                }
            }
        }
    }

    /**
     * The correctness of parse variable
     */
    @Test
    fun parserVarTest() {
        val ast = parserTest("/var/var.xiao")
        val expectedData = listOf(
            ExpectedVariableData("a", "12"),
            ExpectedVariableData("b", "ABC", "Str"),
            ExpectedVariableData("d", "a", "Int")
        )

        ast.mapIndexed { index, astNode ->
            if (astNode is Statement.VariableDeclaration) {
                val expression = astNode.expression
                assertEquals(expectedData[index].name, astNode.variableName.literal)
                assertEquals(expectedData[index].type, astNode.type?.typeTokens?.literal)

                when (expression) {
                    is Expression.CallFunctionExpression -> assertEquals(
                        expectedData[index].value,
                        expression.functionName.literal
                    )
                    is Expression.StringExpression -> assertEquals(expectedData[index].value, expression.value.literal)
                    is Expression.FloatExpression -> assertEquals(expectedData[index].value, expression.value.literal)
                    is Expression.IntExpression -> assertEquals(expectedData[index].value, expression.value.literal)
                    else -> {}
                }
            }
        }
    }

    @Test
    fun parserIfTest() {

    }
}
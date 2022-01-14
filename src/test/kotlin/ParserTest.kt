import xiaoLanguage.ast.*
import xiaoLanguage.ast.Function
import xiaoLanguage.lexer.Lexer
import xiaoLanguage.parser.Parser
import xiaoLanguage.tokens.TokenType
import xiaoLanguage.util.Report
import xiaoLanguage.util.StringStream
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

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

    data class ExpectedOperatorData(
        val expressions: List<String>,
        val operator: List<String>
    )

    data class ExpectedPathData(
        val names: List<String>
    )

    data class ExpectedTokenData(
        val literal: String,
        val type: TokenType
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
            if (report is Report.Error) ast.clear()
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
        val ast = parserTest("/parserTestData/import/import.xiao")

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
        val ast = parserTest("/parserTestData/functions/function.xiao")

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
        val ast = parserTest("/parserTestData/classes/class.xiao")
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
        val ast = parserTest("/parserTestData/var/var.xiao")
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
                    is Expression.CallExpression -> assertEquals(
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

    /**
     * The correctness of parse if statement
     */
    @Test
    fun parserIfStatementTest() {
        val ast = parserTest("/parserTestData/ifStatement/if.xiao")

        assertNotEquals(0, ast.size)
    }

    /**
     * The correctness of parse operator
     */
    @Test
    fun parserOperatorTest() {
        fun getString(expressions: List<Expression>, operatorList: List<String>, index: Int): List<String> {
            val strings = mutableListOf<String>()
            expressions.map { expression ->
                when (expression) {
                    is Expression.IntExpression -> strings += expression.value.literal
                    is Expression.FloatExpression -> strings += expression.value.literal
                    is Expression.StringExpression -> strings += expression.value.literal
                    is Expression.OperatorExpression -> {
                        assertEquals(operatorList[index + 1], expression.operator.operator.literal)
                        getString(expression.operator.expressions, operatorList, index + 1).map { strings += it }
                    }
                    else -> {}
                }
            }

            return strings
        }

        val ast = parserTest("/parserTestData/operators/operator.xiao")
        val expectedData = listOf(
            ExpectedOperatorData(listOf("10", "20", "30", "50"), listOf("+", "+", "+")),
            ExpectedOperatorData(listOf("10", "50", "20", "50"), listOf("/", ">", "<"))
        )

        ast.mapIndexed { index, astNode ->
            astNode as Statement.ExpressionStatement
            val operator = (astNode.expression[0] as Expression.OperatorExpression).operator

            if (operator is Operator.Plus) {
                assertEquals(expectedData[index].operator[0], operator.operator.literal)
                getString(operator.expressions, expectedData[index].operator, 0)
            }
        }
    }

    /**
     * The correctness of parse path
     */
    @Test
    fun parserPathTest() {
        val ast = parserTest("/parserTestData/path/path.xiao")
        val expectedData = listOf(
            ExpectedPathData(
                listOf("test", "test2", "test")
            ),
            ExpectedPathData(
                listOf("test", "test5", "test")
            )
        )

        ast.mapIndexed { index, astNode ->
            astNode as Statement.ExpressionStatement

            astNode.expression.mapIndexed { index2, expression ->
                if (expression is Expression.CallExpression) {
                    assertEquals(expectedData[index].names[index2], expression.functionName.literal)
                } else if (expression is Expression.VariableExpression) {
                    assertEquals(expectedData[index].names[index2], expression.value.literal)
                }
            }
        }
    }

    /**
     * The correctness of parse return statement
     */
    @Test
    fun parserReturnStatementTest() {
        val ast = parserTest("/parserTestData/returnStatement/return.xiao")
        val expectedData = listOf("100", "abc", "A", "a", "a")

        ast.mapIndexed { index, astNode ->
            astNode as Statement.ReturnStatement

            when (val expression = astNode.expression) {
                is Expression.CallExpression -> assertEquals(
                    expectedData[index],
                    expression.functionName.literal
                )
                is Expression.VariableExpression -> assertEquals(expectedData[index], expression.value.literal)
                is Expression.IntExpression -> assertEquals(expectedData[index], expression.value.literal)
                is Expression.FloatExpression -> assertEquals(expectedData[index], expression.value.literal)
                is Expression.StringExpression -> assertEquals(expectedData[index], expression.value.literal)
                else -> {}
            }
        }
    }

    /**
     * The correctness of parse generator
     */
    @Test
    fun parserGeneratorTest() {
        val ast = parserTest("/parserTestData/generator/generator.xiao")
        val expectedData = listOf(
            listOf(
                ExpectedTokenData("10", TokenType.INTEGER_LITERAL_TOKEN),
                ExpectedTokenData("20", TokenType.INTEGER_LITERAL_TOKEN)
            ),
            listOf(
                ExpectedTokenData("2", TokenType.STRING_LITERAL_TOKEN),
                ExpectedTokenData("9", TokenType.STRING_LITERAL_TOKEN)
            ),
            listOf(
                ExpectedTokenData("0.3", TokenType.FLOAT_LITERAL_TOKEN),
                ExpectedTokenData("0.7", TokenType.FLOAT_LITERAL_TOKEN)
            ),
            listOf(
                ExpectedTokenData(".5", TokenType.FLOAT_LITERAL_TOKEN),
                ExpectedTokenData(".3", TokenType.FLOAT_LITERAL_TOKEN)
            ),
            listOf(
                ExpectedTokenData("a", TokenType.IDENTIFIER_TOKEN),
                ExpectedTokenData("b", TokenType.IDENTIFIER_TOKEN)
            )
        )

        ast.mapIndexed { index, astNode ->
            if (astNode is Expression.GeneratorExpression) {
                astNode.value.mapIndexed { index2, token ->
                    assertEquals(expectedData[index][index2].literal, token.literal)
                    assertEquals(expectedData[index][index2].type, token.tokenType)
                }
            }
        }
    }
}
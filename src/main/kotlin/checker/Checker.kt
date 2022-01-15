package xiaoLanguage.checker

import xiaoLanguage.ast.*
import xiaoLanguage.ast.Function
import xiaoLanguage.compiler.Compiler
import xiaoLanguage.exception.*
import xiaoLanguage.util.Report
import java.io.File
import java.util.*
import kotlin.reflect.KMutableProperty1

class Checker(val ast: MutableList<ASTNode>, private val mainFile: File) {
    private val stdPath = ""
    private val checkerReport = mutableListOf<Report>()
    private val asts = mutableMapOf<String, MutableList<ASTNode>>()
    private val hierarchy = mutableListOf<MutableList<ASTNode>>(mutableListOf())

    fun check(): CheckReturnData {
        val checkAST = mutableListOf<ASTNode>()

        for (node in ast) {
            if (node is Import) {
                checkImport(node)
            } else {
                val expression = checkExpressions(node)
                checkAST += expression
                hierarchy[0] += expression
            }
        }

        asts[mainFile.nameWithoutExtension] = checkAST

        return CheckReturnData(asts, checkerReport, hierarchy[0])
    }

    private fun findVarOrFunctionOrClass(name: String, judgmental: (ASTNode) -> Boolean): ASTNode? {
        data class FindData(val info: ASTNode, val priority: Int)

        val data = mutableListOf<FindData>()
        for (i in hierarchy.size - 1 downTo 0) {
            val findData = hierarchy[i].find {
                when (it) {
                    is Function -> it.functionName.literal == name && judgmental(it)
                    is Class -> it.className.literal == name && judgmental(it)
                    is Statement.VariableDeclaration -> it.variableName.literal == name && judgmental(it)
                    else -> false
                }
            }

            if (findData != null) data += FindData(findData, i)
        }

        return data.maxWithOrNull(compareBy { it.priority })?.info
    }

    private fun checkExpressions(node: ASTNode): ASTNode {
        return when (node) {
            is Statement -> checkStatement(node)
            is Function -> checkFunction(node)
            is Class -> checkClass(node)
            is Expression -> checkExpress(node)
            else -> node
        }
    }

    private fun checkExpress(node: Expression): Expression = when (node) {
        is Expression.CallExpression -> checkCallFunction(node)
        is Expression.ReSetVariableExpression -> checkReSetVariableValue(node)
        else -> node
    }

    private fun checkStatement(statement: Statement): Statement {
        return when (statement) {
            is Statement.VariableDeclaration -> {
                val upHierarchy = hierarchy[hierarchy.size - 1]
                val upHierarchyVariable = upHierarchy.filterIsInstance<Statement.VariableDeclaration>()

                checkVariableStatement(
                    statement, upHierarchyVariable.size + 1, upHierarchyVariable
                )

                hierarchy[hierarchy.size - 1] += statement
                statement
            }
            is Statement.ExpressionStatement -> checkExpressionStatement(statement)
            else -> statement
        }
    }

    private fun checkImport(node: Import) {
        val path = node.path.joinToString("/") { it.literal }
        var file = File("${mainFile.absoluteFile.parent}/$path.xiao")

        if (!file.exists()) {
            file = File("$stdPath/$path.xiao")

            if (!file.exists()) checkerReport += Report.Error(
                ModuleNotFoundError("No module named '${node.path.joinToString(".") { it.literal }}'"),
                Report.Code(
                    node.importKeyword.position.lineNumber,
                    node.path[0].position,
                    arrowEnd = node.path[node.path.size - 1].position
                )
            )
            else {
                val (ast, value) = Compiler(file).compile()
                hierarchy[0] += Check.ImportFileValue(
                    node.path[node.path.size - 1].literal,
                    value.filterIsInstance<Function>()
                )
                asts[path] = ast[file.nameWithoutExtension]!!
            }
        }
    }

    private fun checkClass(node: Class): Class {
        if (node.className.literal[0].isLowerCase()) checkerReport += Report.Warning(
            NamingRulesError("Class naming rules error."),
            Report.Code(node.className.position.lineNumber, node.className.position),
            help = listOf(
                Report.Help(
                    """
                    |class ${node.className.literal.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} {}
                    """,
                    "correct naming rule capitalize."
                )
            )
        )
        if (hierarchy[hierarchy.size - 1].filterIsInstance<Class>()
                .find { it.className.literal == node.className.literal } == null
        ) {
            hierarchy[hierarchy.size - 1] += node
            hierarchy.add(mutableListOf())

            node.variables.map { variableDeclaration ->
                checkStatement(variableDeclaration)
            }

            node.functions.map { function ->
                checkExpressions(function)
            }

            hierarchy.removeAt(hierarchy.size - 1)
        } else checkerReport += Report.Error(
            SyntaxError("Identifier '${node.className.literal}' has already been declared"),
            Report.Code(node.className.position.lineNumber, node.className.position)
        )

        return node
    }

    private fun checkFunction(node: Function): Function {
        if (hierarchy[hierarchy.size - 1].filterIsInstance<Function>()
                .find { it.functionName.literal == node.functionName.literal } == null
        ) {
            hierarchy[hierarchy.size - 1] += node

            hierarchy.add(mutableListOf())
            node.parameters.mapIndexed { index, parameter ->
                hierarchy[hierarchy.size - 1] += Check.ParameterValue(
                    parameter.name.literal,
                    parameter.type,
                    index + 1
                )
            }
            node.statements.map { statement ->
                checkExpressions(statement)
                hierarchy[hierarchy.size - 1] += statement
                statement
            }

            hierarchy.removeAt(hierarchy.size - 1)
        } else checkerReport += Report.Error(
            SyntaxError("Identifier '${node.functionName.literal}' has already been declared"),
            Report.Code(node.functionName.position.lineNumber, node.functionName.position)
        )

        return node
    }

    private fun checkVariableStatement(
        node: Statement.VariableDeclaration, id: Int, variables: List<Statement.VariableDeclaration>
    ): Statement.VariableDeclaration {
        if (variables.find { it.variableName.literal == node.variableName.literal } == null) {
            node.findId = id

            node.type = when (node.expression) {
                is Expression.StringExpression -> Type(listOf(), 0, "Str")

                else -> Type(listOf(), 0, "")
            }
        } else checkerReport += Report.Error(
            SyntaxError("Variable '${node.variableName.literal}' has already been declared"),
            Report.Code(node.position.lineNumber, node.position)
        )

        return node
    }

    private fun checkExpressionStatement(node: Statement.ExpressionStatement): Statement.ExpressionStatement {
        val expressions = node.expression

        if (expressions.size == 1) {
            checkExpress(expressions[0])
        }

        return node
    }

    private fun checkCallFunction(
        node: Expression.CallExpression
    ): Expression.CallExpression {
        for (layers in (hierarchy.size - 1) downTo 0) {
            val function =
                hierarchy[layers].find {
                    (it is Function && it.functionName.literal == node.name.literal) ||
                            (it is Statement.VariableDeclaration && it.type?.type == "function") ||
                            (it is Class && it.className.literal == node.name.literal)
                }

            if (function != null) {
                val parameters = when (function) {
                    is Function -> function.parameters
//                    is Statement.VariableDeclaration -> {
//                        val expression = function.expression as Expression.VariableExpression
//                        (findVarOrFunctionOrClass(expression.value.literal, Function::class.java) as Function).parameters
//                    }
                    else -> listOf()
                }

                if (parameters.size != node.args.size) {
                    checkerReport += if (parameters.size - node.args.size > 0) {
                        val missing = parameters.size - node.args.size
                        Report.Error(
                            TypeError(
                                "${node.name.literal}() missing $missing required positional arguments: " +
                                        parameters.filterIndexed { index, _ ->
                                            index > parameters.size - missing - 1
                                        }.joinToString(" and ") { it.name.literal }
                            ),
                            Report.Code(node.name.position.lineNumber, node.name.position)
                        )
                    } else Report.Error(
                        TypeError("take ${parameters.size} positional arguments but ${node.args.size} were given"),
                        Report.Code(node.name.position.lineNumber, node.name.position)
                    )
                } else {
                    // TODO check type
                }

                return node
            }
        }

        checkerReport += Report.Error(
            NameError("name '${node.name.literal}' function is not defined"),
            Report.Code(node.name.position.lineNumber, node.name.position)
        )

        return node
    }

    private fun checkReSetVariableValue(node: Expression.ReSetVariableExpression): Expression.ReSetVariableExpression {
        val variable =
            findVarOrFunctionOrClass(node.variableName.literal) { it is Statement.VariableDeclaration } as Statement.VariableDeclaration
        if (variable.mutKeyword == null) checkerReport += Report.Error(
            TypeError("Cannot assign twice to immutable variable"),
            Report.Code(node.variableName.position.lineNumber, node.variableName.position),
            help = listOf(
                Report.Help(
                    """
                        |var mut ${node.variableName.literal} = ...
                    """,
                    "make ${node.variableName.literal} mutable"
                )
            )
        )
        return node
    }
}
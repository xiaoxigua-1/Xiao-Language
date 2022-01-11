package xiaoLanguage.checker

import xiaoLanguage.ast.*
import xiaoLanguage.ast.Function
import xiaoLanguage.compiler.Compiler
import xiaoLanguage.exception.*
import xiaoLanguage.util.Report
import java.io.File

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
                val expression = checkExpressions(node, hierarchy)
                checkAST += expression
                hierarchy[0] += expression
            }
        }

        asts[mainFile.nameWithoutExtension] = checkAST

        return CheckReturnData(asts, checkerReport, hierarchy[0])
    }

    private fun checkExpressions(node: ASTNode, variableHierarchy: MutableList<MutableList<ASTNode>>): ASTNode {
        return when (node) {
            is Statement -> checkStatement(node, variableHierarchy)
            is Function -> checkFunction(node, variableHierarchy)
            is Class -> checkClass(node, variableHierarchy)
            is Expression -> checkExpress(node)
            else -> node
        }
    }

    private fun checkExpress(node: Expression): Expression = when (node) {
        is Expression.CallFunctionExpression -> checkCallFunction(node)
        else -> node
    }

    private fun checkStatement(statement: Statement, variableHierarchy: MutableList<MutableList<ASTNode>>): Statement {
        return when (statement) {
            is Statement.VariableDeclaration -> {
                val upHierarchy = variableHierarchy[variableHierarchy.size - 1]
                val upHierarchyVariable = upHierarchy.filterIsInstance<Statement.VariableDeclaration>()

                checkVariableStatement(
                    statement, upHierarchyVariable.size + 1, upHierarchyVariable
                )

                variableHierarchy[variableHierarchy.size - 1] += statement
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
                node.path[0].position,
                node.path[node.path.size - 1].position
            )
            else {
                val (ast, value) = Compiler(file).compile()
                hierarchy[0] += ImportFileValue(
                    node.path[node.path.size - 1].literal,
                    value.filterIsInstance<Function>()
                )
                asts[path] = ast[file.nameWithoutExtension]!!
            }
        }
    }

    private fun checkClass(node: Class, variableHierarchy: MutableList<MutableList<ASTNode>>): Class {
        if (node.className.literal[0].isLowerCase()) checkerReport += Report.Warning(
            NamingRulesError("Class naming rules error."),
            node.className.position,
            hint = "correct naming rule 'class Example {}'"
        )
        if (variableHierarchy[variableHierarchy.size - 1].filterIsInstance<Class>()
                .find { it.className.literal == node.className.literal } == null
        ) {
            variableHierarchy[variableHierarchy.size - 1] += node
            variableHierarchy.add(mutableListOf())

            node.functions.map { function ->
                checkExpressions(function, variableHierarchy)
                function
            }

            variableHierarchy.removeAt(variableHierarchy.size - 1)
        } else checkerReport += Report.Error(
            SyntaxError("Identifier '${node.className.literal}' has already been declared"),
            node.className.position
        )

        return node
    }

    private fun checkFunction(node: Function, variableHierarchy: MutableList<MutableList<ASTNode>>): Function {
        if (variableHierarchy[variableHierarchy.size - 1].filterIsInstance<Function>()
                .find { it.functionName.literal == node.functionName.literal } == null
        ) {
            variableHierarchy[variableHierarchy.size - 1] += node

            variableHierarchy.add(mutableListOf())
            node.statements.map { statement ->
                checkExpressions(statement, variableHierarchy)
                variableHierarchy[variableHierarchy.size - 1] += statement
                statement
            }

            variableHierarchy.removeAt(variableHierarchy.size - 1)
        } else checkerReport += Report.Error(
            SyntaxError("Identifier '${node.functionName.literal}' has already been declared"),
            node.functionName.position
        )

        return node
    }

    private fun checkVariableStatement(
        node: Statement.VariableDeclaration, id: Int, variables: List<Statement.VariableDeclaration>
    ): Statement.VariableDeclaration {
        if (variables.find { it.variableName.literal == node.variableName.literal } == null) {
            node.findId = id
        } else checkerReport += Report.Error(
            SyntaxError("Variable '${node.variableName.literal}' has already been declared"),
            node.position
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
        node: Expression.CallFunctionExpression
    ): Expression.CallFunctionExpression {
        for (layers in (hierarchy.size - 1) downTo 0) {
            val function =
                hierarchy[layers].find { it is Function && it.functionName.literal == node.functionName.literal }

            if (function != null) {
                val parameters = (function as Function).parameters

                if (parameters.size != node.args.size) {
                    checkerReport += if (parameters.size - node.args.size > 0) {
                        val missing = parameters.size - node.args.size
                        Report.Error(
                            TypeError(
                                "${node.functionName.literal}() missing $missing required positional arguments: " +
                                        parameters.filterIndexed { index, _ ->
                                            index > parameters.size - missing - 1
                                        }.joinToString(" and ") { it.name.literal }
                            ),
                            node.functionName.position
                        )
                    } else Report.Error(
                        TypeError("take ${parameters.size} positional arguments but ${node.args.size} were given"),
                        node.functionName.position
                    )
                } else {
                    // TODO check type
                }

                return node
            }
        }

        checkerReport += Report.Error(
            NameError("name '${node.functionName.literal}' function is not defined"),
            node.functionName.position
        )

        return node
    }
}
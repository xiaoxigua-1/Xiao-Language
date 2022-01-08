package xiaoLanguage.checker

import xiaoLanguage.ast.ASTNode
import xiaoLanguage.ast.Class
import xiaoLanguage.ast.Function
import xiaoLanguage.ast.Import
import xiaoLanguage.ast.Statement
import xiaoLanguage.compiler.Compiler
import xiaoLanguage.exception.ModuleNotFoundError
import xiaoLanguage.exception.SyntaxError
import xiaoLanguage.util.Report
import java.io.File

class Checker(val ast: MutableList<ASTNode>, private val mainFile: File) {
    private val checkerReport = mutableListOf<Report>()
    private val asts = mutableMapOf<String, MutableList<ASTNode>>()
    private val hierarch = mutableListOf<MutableList<ASTNode>>(mutableListOf())

    fun check(): Pair<MutableMap<String, MutableList<ASTNode>>, List<Report>> {
        val checkAST = mutableListOf<ASTNode>()

        for (node in ast) {
            if (node is Import) {
                checkImport(node)
            } else {
                val expression = checkExpressions(node, hierarch)
                checkAST += expression
                hierarch[0] += expression
            }
        }

        asts[mainFile.nameWithoutExtension] = checkAST

        return asts to checkerReport
    }

    private fun checkExpressions(node: ASTNode, variableHierarchy: MutableList<MutableList<ASTNode>>): ASTNode {
        return when (node) {
            is Statement -> checkStatement(node, variableHierarchy)
            is Function -> checkFunction(node, variableHierarchy)
            is Class -> checkClass(node, variableHierarchy)
            else -> node
        }
    }

    private fun checkStatement(statement: Statement, variableHierarchy: MutableList<MutableList<ASTNode>>): Statement {
        return when (statement) {
            is Statement.VariableDeclaration -> {
                val upHierarchy = variableHierarchy[variableHierarchy.size - 1]
                val upHierarchyVariable = upHierarchy.filterIsInstance<Statement.VariableDeclaration>()

                checkVariable(
                    statement,
                    upHierarchyVariable.size + 1,
                    upHierarchyVariable
                )

                variableHierarchy[variableHierarchy.size - 1] += statement
                statement
            }

            else -> statement
        }
    }

    private fun checkImport(node: Import) {
        val path = when (node.path[0].literal) {
            "xiao" -> {
                ""
            }
            else -> {
                node.path.joinToString("/") { it.literal }
            }
        }
        val file = File("${mainFile.absoluteFile.parent}/$path.xiao")

        if (!file.exists()) checkerReport += Report.Error(
            ModuleNotFoundError("No module named '${node.path.joinToString(".") { it.literal }}'"),
            node.path[0].position,
            node.path[node.path.size - 1].position
        )
        else asts[path] = Compiler(file).compile()[file.nameWithoutExtension]!!
    }

    private fun checkClass(node: Class, variableHierarchy: MutableList<MutableList<ASTNode>>): Class {

        return node
    }

    private fun checkFunction(node: Function, variableHierarchy: MutableList<MutableList<ASTNode>>): Function {
        variableHierarchy.add(mutableListOf())
        node.statements.map { statement ->
            val variables = variableHierarchy[variableHierarchy.size - 1].filterIsInstance<Statement.VariableDeclaration>()

            when (statement) {
                is Statement.VariableDeclaration -> {
                    checkVariable(statement, variables.size + 1, variables)
                    variableHierarchy[variableHierarchy.size - 1] += statement
                    node
                }
                else -> statement
            }

            variableHierarchy.removeAt(variableHierarchy.size - 1)
        }

        return node
    }

    private fun checkVariable(
        node: Statement.VariableDeclaration,
        id: Int,
        variables: List<Statement.VariableDeclaration>
    ): Statement.VariableDeclaration {
        if (variables.find { it.variableName.literal == node.variableName.literal } == null) {
            node.findId = id
        } else {
            checkerReport += Report.Error(
                SyntaxError("Identifier '${node.variableName.literal}' has already been declared"),
                node.position,
                node.position
            )
        }

        return node
    }
}
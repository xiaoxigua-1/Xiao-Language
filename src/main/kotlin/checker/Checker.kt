package xiaoLanguage.checker

import xiaoLanguage.ast.ASTNode
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
    private val globalVariableId = mutableListOf<String>()

    fun check(): Pair<MutableMap<String, MutableList<ASTNode>>, List<Report>> {
        val checkAST = mutableListOf<ASTNode>()

        for (node in ast) {
            if (node is Import) {
                checkImport(node)
            } else checkAST += checkExpressions(node)
        }

        asts[mainFile.nameWithoutExtension] = checkAST

        return asts to checkerReport
    }

    private fun checkExpressions(node: ASTNode): ASTNode {
        return when (node) {
            is Statement -> checkStatement(node)
            is Function -> checkFunction(node)
            else -> node
        }
    }

    private fun checkStatement(statement: Statement): Statement {
        return when (statement) {
            is Statement.VariableDeclaration -> {
                checkVariable(statement, globalVariableId.size + 1, globalVariableId)
                globalVariableId += statement.variableName.literal
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

    private fun checkFunction(node: Function): Function {
        val variableId = mutableListOf<String>()

        node.statements.map { statement ->
            when (statement) {
                is Statement.VariableDeclaration -> {
                    checkVariable(statement, variableId.size + 1, variableId)
                    variableId += statement.variableName.literal
                    node
                }
                else -> statement
            }
        }

        return node
    }

    private fun checkVariable(
        node: Statement.VariableDeclaration,
        id: Int,
        variables: List<String>
    ): Statement.VariableDeclaration {
        if (variables.find { it == node.variableName.literal }.isNullOrEmpty()) {
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
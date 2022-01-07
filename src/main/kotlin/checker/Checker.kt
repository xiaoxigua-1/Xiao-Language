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

    fun check(): Pair<MutableMap<String, MutableList<ASTNode>>, List<Report>> {
        val checkAST = mutableListOf<ASTNode>()
        val globalVariableId = mutableListOf<String>()

        for (node in ast) {
            if (node is Import) {
                checkImport(node)
            } else checkAST += when (node) {
                is Function -> checkFunction(node)
                is Statement.VariableDeclaration -> {
                    checkVariable(node, globalVariableId.size + 1, globalVariableId)
                    globalVariableId += node.variableName.literal
                    node
                }
                else -> node
            }
        }

        asts[mainFile.nameWithoutExtension] = checkAST

        return asts to checkerReport
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
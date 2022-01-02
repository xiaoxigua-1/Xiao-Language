package xiaoLanguage.checker

import xiaoLanguage.ast.ASTNode
import xiaoLanguage.ast.Import
import xiaoLanguage.compiler.Compiler
import xiaoLanguage.util.Report
import java.io.File

class Checker(val ast: MutableList<ASTNode>, private val mainFile: File) {
    private val checkerReport = mutableListOf<Report>()
    private val asts = mutableMapOf<String, MutableList<ASTNode>>()

    fun check(): MutableMap<String, MutableList<ASTNode>> {
        val checkAST = mutableListOf<ASTNode>()

        for (node in ast) {
            when (node) {
                is Import -> checkImport(node)
                else -> checkAST += node
            }
        }

        asts[mainFile.nameWithoutExtension] = checkAST

        return asts
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

        asts[path] = Compiler(file).compile()[file.nameWithoutExtension]!!
    }
}
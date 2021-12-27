package xiaoLanguage.checker

import xiaoLanguage.ast.Import
import xiaoLanguage.ast.Expression
import xiaoLanguage.compiler.Compiler
import xiaoLanguage.util.Report
import java.io.File

class Checker(val ast: MutableList<Expression>, val mainFilePath: String) {
    private val checkerReport = mutableListOf<Report>()
    private val asts = mutableListOf<Expression>()

    fun check(): MutableList<Expression> {
        for (node in ast) {
            when (node) {
                is Import -> checkImport(node)
                else -> asts += node
            }
        }

        return asts
    }

    private fun checkImport(node: Import) {
        val path = when (node.path[0].literal) {
            "xiao" -> {
                ""
            }
            else -> {
                node.path.joinToString("") { it.literal }
            }
        }

        val file = File("$mainFilePath/$path.xiao")

        asts += Compiler(file).compile()
    }
}
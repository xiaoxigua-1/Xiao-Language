package xiaoLanguage.checker

import xiaoLanguage.ast.Import
import xiaoLanguage.ast.Expression
import xiaoLanguage.compiler.Compiler
import xiaoLanguage.util.Report
import java.io.File

class Checker(val ast: MutableList<Expression>) {
    private val checkerReport = mutableListOf<Report>()
    private val asts = mutableListOf<MutableList<Expression>>()

    fun check(): MutableList<MutableList<Expression>> {
        for (node in ast) {
            when (node) {
                is Import -> checkImport(node)
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

        asts += Checker(Compiler(File(path)).compile()).check()
    }
}
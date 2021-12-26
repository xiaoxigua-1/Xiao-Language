package xiaoLanguage.checker

import xiaoLanguage.ast.Import
import xiaoLanguage.ast.Expression
import xiaoLanguage.compiler.Compiler
import xiaoLanguage.util.Report
import java.io.File

class Checker(val ast: MutableList<Expression>) {
    private val checkerReport = mutableListOf<Report>()

    fun check() {
        for (node in ast) {
            when (node) {
                is Import -> checkImport(node)
            }
        }
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

        Compiler(File(path))
    }
}
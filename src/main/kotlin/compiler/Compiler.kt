package xiaoLanguage.compiler

import xiaoLanguage.ast.Expression
import xiaoLanguage.checker.Checker
import xiaoLanguage.lexer.Lexer
import xiaoLanguage.parser.Parser
import xiaoLanguage.util.StringStream
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.name

class Compiler(private val file: File) {
    fun compile(): MutableMap<String, MutableList<Expression>> {
        val stringStream = StringStream(file)
        val lex = Lexer(stringStream)
        val parser = Parser(lex, file)

        val (ast, report) = parser.parser()

        report.forEach {
            it.printReport(file.readLines(), file.absolutePath)
        }

        return Checker(ast, file.absoluteFile.parent).check()
    }
}
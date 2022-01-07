package xiaoLanguage.compiler

import xiaoLanguage.ast.ASTNode
import xiaoLanguage.checker.Checker
import xiaoLanguage.lexer.Lexer
import xiaoLanguage.parser.Parser
import xiaoLanguage.util.StringStream
import java.io.File

class Compiler(private val file: File) {
    fun compile(): MutableMap<String, MutableList<ASTNode>> {
        val stringStream = StringStream(file)
        val lex = Lexer(stringStream)
        val parser = Parser(lex, file)

        val (ast, parserReport) = parser.parser()

        parserReport.forEach {
            it.printReport(file.readLines(), file.absolutePath)
        }
        val (structure, checkerReport) = Checker(ast, file).check()
        checkerReport.forEach {
            it.printReport(file.readLines(), file.absolutePath)
        }
        return structure
    }
}
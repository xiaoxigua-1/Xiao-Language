package xiaoLanguage.compiler

import xiaoLanguage.ast.ASTNode
import xiaoLanguage.checker.Checker
import xiaoLanguage.lexer.Lexer
import xiaoLanguage.parser.Parser
import xiaoLanguage.util.StringStream
import java.io.File

class Compiler(val mainFile: File) {
    fun init(): Pair<MutableMap<String, MutableList<ASTNode>>, List<ASTNode>> = compile(mainFile)

    fun compile(file: File): Pair<MutableMap<String, MutableList<ASTNode>>, List<ASTNode>> {
        val stringStream = StringStream(file)
        val lex = Lexer(stringStream)
        val parser = Parser(lex, file)

        val (ast, parserReport) = parser.parser()

        parserReport.forEach {
            it.printReport(file.readLines(), file.absolutePath)
        }

        val (structure, checkerReport, global) = Checker(ast, file, this).check()

        checkerReport.forEach {
            it.printReport(file.readLines(), file.absolutePath)
        }

        return structure to global
    }
}
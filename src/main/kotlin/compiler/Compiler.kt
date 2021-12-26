package xiaoLanguage.compiler

import xiaoLanguage.lexer.Lexer
import xiaoLanguage.parser.Parser
import xiaoLanguage.util.StringStream
import java.io.File

class Compiler(private val file: File) {
    fun compile(): MutableList<xiaoLanguage.ast.Expression> {
        val stringStream = StringStream(file)
        val lex = Lexer(stringStream)
        val parser = Parser(lex, file)

        val (ast, report) = parser.parser()

        report.forEach {
            it.printReport(file.readLines(), file.absolutePath)
        }

        return ast
    }
}
package xiaoLanguage.compiler

import xiaoLanguage.lexer.Lexer
import xiaoLanguage.parser.Parser
import xiaoLanguage.util.StringStream
import java.io.File

class Compiler(private val file: File) {
    fun compile() {
        val stringStream = StringStream(file)
        val tokens = Lexer(stringStream).lex()
        val parser = Parser(tokens)

    }
}
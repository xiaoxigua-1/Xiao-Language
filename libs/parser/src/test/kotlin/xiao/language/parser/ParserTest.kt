package xiao.language.parser

import xiao.language.lexer.FileStream
import xiao.language.lexer.Lexer
import xiao.language.utilities.ast.Statement
import kotlin.test.Test

class ParserTest {
    private val functionsFile = Parser::class.java.getResource("/functions.xiao")!!

    @Test
    fun parserFunctionTest() {
        val lexer = Lexer(FileStream(functionsFile))
        val parser = Parser(lexer)
        for (function in parser) {
            println(function)
            assert(function is Statement.Function)
        }
    }
}

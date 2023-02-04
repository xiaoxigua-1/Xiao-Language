package xiao.language.parser

import xiao.language.lexer.FileStream
import xiao.language.lexer.Lexer
import xiao.language.utilities.ast.Statement
import kotlin.test.Test

class ParserTest {
    @Test
    fun parserFunctionTest() {
        val lexer = Lexer(FileStream("fn Test::test() {\n{test();}; \nTest::test; \n}"))
        val parser = Parser(lexer)
        val function = parser.next()
        assert(function is Statement.Function)
        println(parser.hasNext())
    }
}

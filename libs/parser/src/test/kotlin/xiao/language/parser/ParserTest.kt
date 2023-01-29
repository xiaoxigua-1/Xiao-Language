package xiao.language.parser

import xiao.language.lexer.FileStream
import xiao.language.lexer.Lexer
import java.io.File
import kotlin.test.Test

class ParserTest {
    @Test fun parserFunctionTest() {
        val lexer = Lexer(FileStream("fn Test::test()"))
        val parser = Parser(lexer)
        parser.statements()
    }
}

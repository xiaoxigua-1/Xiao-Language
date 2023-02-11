package xiao.language.parser

import xiao.language.lexer.FileStream
import xiao.language.lexer.Lexer
import xiao.language.utilities.Span
import xiao.language.utilities.ast.Statement
import xiao.language.utilities.exceptions.Exceptions
import xiao.language.utilities.report.Level
import xiao.language.utilities.report.Report
import xiao.language.utilities.report.ReportPrint
import xiao.language.utilities.report.all
import kotlin.test.Test

class ParserTest {
    private val functionsFile = Parser::class.java.getResource("/functions.xiao")!!

    @Test
    fun parserFunctionTest() {
        val lexer = Lexer(FileStream(functionsFile))
        val parser = Parser(lexer)
        try {
            for (function in parser) {
                println(function)
                assert(function is Statement.Function)
                ReportPrint(listOf(Report("", Level.Error, function.span)), functionsFile.readText(), functionsFile.path).all()
            }
        } catch (e: Exceptions.ExpectException) {
            println(e.span)
            ReportPrint(listOf(Report(e, Level.Error)), functionsFile.readText(), functionsFile.path).all()
        }
    }
}

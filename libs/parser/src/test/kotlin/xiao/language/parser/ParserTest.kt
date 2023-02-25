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
        val reports = mutableListOf<Report>()
        try {
            for (function in parser) {
                assert(function is Statement.Function)
                reports += Report("Debug", Level.Debug, function.span)
            }
        } catch (e: Exceptions.ExpectException) {
            reports += Report(e, Level.Error)
        }

        ReportPrint(reports, functionsFile.readText(), functionsFile.path).all()
    }
}

import xiaoLanguage.checker.Checker
import xiaoLanguage.compiler.Compiler
import java.io.File
import kotlin.test.Test

class CheckerTest {
    @Test
    fun checkerTest() {
        val file = File(this::class.java.getResource("/test.xiao").path)
        val ast = Compiler(file).compile()
        println(ast)
    }
}
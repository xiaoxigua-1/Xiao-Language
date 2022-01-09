import xiaoLanguage.ast.ASTNode
import xiaoLanguage.ast.Statement
import xiaoLanguage.compiler.Compiler
import java.io.File
import kotlin.test.Test

class CheckTest {
    private fun checkTest(path: String): MutableMap<String, MutableList<ASTNode>> {
        val file = File(this::class.java.getResource(path)!!.path)
        return Compiler(file).compile().first
    }

    @Test
    fun variableFindIdTest() {
        val structure = checkTest("/checkerTestData/all.xiao")
        val varFileAST = structure["parserTestData/var"]

        varFileAST?.forEach {
            if (it is Statement.VariableDeclaration) {
                println(it.findId)
            }
        }
    }
}
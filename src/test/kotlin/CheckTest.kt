import xiaoLanguage.ast.ASTNode
import xiaoLanguage.ast.Statement
import xiaoLanguage.compiler.Compiler
import java.io.File
import kotlin.test.Test

class CheckTest {
    private fun checkTest(path: String): MutableMap<String, MutableList<ASTNode>> {
        val file = File(this::class.java.getResource(path)!!.path)
        return Compiler(file).compile()
    }

    @Test
    fun variableFindIdTest() {
        val structure = checkTest("/var/var.xiao")
        val varFileAST = structure["var"]

        varFileAST?.forEach {
            if (it is Statement.VariableDeclaration) {
                println(it.findId)
            }
        }
    }
}
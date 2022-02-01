package xiaoLanguage.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.path
import xiaoLanguage.bytecode.Bytecode
import xiaoLanguage.compiler.Compiler
import kotlin.io.path.Path
import kotlin.io.path.pathString

class Build : CliktCommand(help = "compile xiao language") {
    private val source by argument().file(mustExist = true).multiple()

    private val output by option("-o").path().default(Path("./build"))

    override fun run() {
        for (file in source) {
            Bytecode(Compiler(file).init().first, file, output.pathString).toByte()
        }
    }
}
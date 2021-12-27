package xiaoLanguage.cli

import xiaoLanguage.checker.Checker
import xiaoLanguage.exception.CommandError
import xiaoLanguage.compiler.Compiler
import java.io.File

class CommandLineInterface(private val args: Array<String>) {
    init {
        if (args.isNotEmpty()) run()
        else commandRun()
    }

    enum class Subcommand(val str: String) {
        BUILD("build"),
        RUN("run")
    }

    private fun run() {
        val subcommand = args[0]

        if (subcommand in Subcommand.values().map { it.str }) {
            when (subcommand) {
                Subcommand.RUN.str -> commandRun()
                Subcommand.BUILD.str -> commandBuild()
            }
        } else throw CommandError("command '$subcommand' is not defined")
    }

    private fun commandBuild() {
        if (args.size > 1) {
            val filePath = args[1]
            Compiler(File(filePath)).compile()
        } else throw CommandError("missing file path")
    }

    private fun commandRun() {

    }
}
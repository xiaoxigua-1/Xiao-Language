package xiaoLanguage

import com.github.ajalt.clikt.core.subcommands
import xiaoLanguage.cli.Build
import xiaoLanguage.cli.CommandLineInterface

fun main(args: Array<String>) = CommandLineInterface().subcommands(Build()).main(args)
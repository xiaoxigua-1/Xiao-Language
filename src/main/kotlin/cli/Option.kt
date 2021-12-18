package xiaoLanguage.cli

import java.io.File

class Option(private val name: String) {
    val names = mutableListOf("--$name")

    fun alias(vararg name: String) {
        name.map {
            names.add(it)
        }
    }

    inline fun <reified T> value(args: Array<String>) {
        names.forEach {
            val index = args.slice(1 until args.size).indexOf(it)
        }
    }
}
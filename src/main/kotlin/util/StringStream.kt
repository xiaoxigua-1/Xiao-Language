package xiaoLanguage.util

import java.io.File

class StringStream(private val file: File) {
    private val fileContent = file.readText()
    var index = 0

    fun nextChar() {
        index += 1
    }

    fun backChar() {
        index -= 1
    }

    val currently: String
        get() = fileContent[index].toString()

    val isEOF: Boolean
        get() = index > fileContent.length - 1

}
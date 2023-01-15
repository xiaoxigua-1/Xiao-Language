package xiao.language.lexer

import java.io.File
import java.net.URI
import java.net.URL

class FileStream(
    private val file: File,
) : Iterator<Char> {
    private var content: String = ""
    private var index: Int = 0
    val current: Char
        get() = content[index - 1]

    constructor(
        uri: URI
    ) : this(File(uri))

    constructor (
        path: String,
    ) : this(File(path))

    constructor (
        path: URL,
    ) : this(File(path.toURI()))

    init {
        this.content = file.readText()
    }

    override fun hasNext() = index < content.length

    override fun next() = content[index++]

    fun peek(): Char? = if (hasNext()) content[index] else null

    fun getIndex(): Int = index - 1
}
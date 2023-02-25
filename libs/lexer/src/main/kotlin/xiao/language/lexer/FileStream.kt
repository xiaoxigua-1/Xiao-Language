package xiao.language.lexer

import java.io.File
import java.net.URI
import java.net.URL

class FileStream(
    private var content: String,
) : Iterator<Char> {
    var index: Int = 0
        private set
    val current: Char
        get() = content[index - 1]

    constructor(
        uri: URI
    ) : this(File(uri).readText())

    constructor (
        path: URL,
    ) : this(File(path.toURI()).readText())

    override fun hasNext() = index < content.length

    override fun next() = content[index++]

    fun peek(): Char? = if (hasNext()) content[index] else null
}
package xiao.language.utilities

fun Char.isAsciiSymbol(): Boolean {
    return when (this) {
        in '!'..'/', in ':'..'@', in '['..'`', in '{'..'~' -> true
        else -> false
    }
}

fun Char.asEscaped(): Char {
    return when (this) {
        'n' -> '\n'
        'r' -> '\r'
        't' -> '\t'
        '0' -> '\u0000'
        'a' -> '\u0007'
        else -> this
    }
}

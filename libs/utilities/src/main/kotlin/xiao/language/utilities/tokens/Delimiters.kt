package xiao.language.utilities.tokens

enum class Delimiters(val delimiter: Char) {
    LeftCurlyBraces('{'),
    RightCurlyBraces('}'),
    LeftSquareBrackets('['),
    RightSquareBrackets(']'),
    LeftParentheses('('),
    RightParentheses(')');

    companion object {
        val delimiters = Delimiters.values().map { it.delimiter }
        private val delimitersMap = Delimiters.values().associateBy { it.delimiter }

        fun fromDelimiters(delim: Char): Delimiters = delimitersMap[delim] ?: throw UnknownError("Unknown delimiter")
    }
}
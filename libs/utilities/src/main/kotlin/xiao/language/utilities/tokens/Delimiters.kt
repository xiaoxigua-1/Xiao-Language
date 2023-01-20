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

        fun fromDelimiters(delim: Char): Delimiters = when (delim) {
            '{' -> LeftCurlyBraces
            '}' -> RightCurlyBraces
            '[' -> LeftSquareBrackets
            ']' -> RightSquareBrackets
            '(' -> LeftParentheses
            ')' -> RightParentheses
            else -> throw UnknownError("Unknown delimiter")
        }
    }
}
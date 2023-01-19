package xiao.language.utilities

enum class Delimiters(val delimiter: Char) {
    LeftCurlyBraces('{'),
    RightCurlyBraces('}'),
    LeftSquareBrackets('['),
    RightSquareBrackets(']'),
    LeftParentheses('('),
    RightParentheses(')');

    companion object {
        val delimiters = Delimiters.values().map { it.delimiter }
    }
}
package xiaoLanguage.tokens

enum class Tokens(val token: String) {
    COLON_TOKEN(":"),
    COMMA_TOKEN(","),
    LEFT_CURLY_BRACKETS_TOKEN("{"),
    RIGHT_CURLY_BRACKETS_TOKEN("}"),
    LEFT_SQUARE_BRACKETS_TOKEN("["),
    RIGHT_SQUARE_BRACKETS_TOKEN("]"),
    DOUBLE_QUOTES_TOKEN("\""),
    SINGLE_QUOTES_TOKEN("'"),
    LEFT_PARENTHESES_TOKEN("("),
    RIGHT_PARENTHESES_TOKEN(")"),
    SLASH_TOKEN("/"),
    BACKSLASH_TOKEN("\\"),
    PLUS_TOKEN("+"),
    MINUS_TOKEN("-"),
    MULTIPLY_TOKEN("*"),
    EQUAL_TOKEN("="),
    DOT_TOKEN("."),
    LESS_TOKEN("<"),
    MORE_TOKEN(">")
}
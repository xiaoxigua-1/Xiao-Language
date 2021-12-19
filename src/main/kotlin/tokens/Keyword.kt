package xiaoLanguage.tokens

enum class Keyword(val keyword: String) {
    CLASS_KEYWORD("class"),
    FUNCTION_KEYWORD("fn"),
    DATA_CLASS_KEYWORD("data"),
    VARIABLE_KEYWORD("var"),
    IF_KEYWORD("if"),
    ELSE_KEYWORD("else"),
    WHILE_KEYWORD("while"),
    FOR_KEYWORD("for")
}
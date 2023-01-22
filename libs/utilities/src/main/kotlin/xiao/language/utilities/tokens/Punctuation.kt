package xiao.language.utilities.tokens

enum class Punctuations(val punctuation: String) {
    Plus("+"),
    Minus("-"),
    Slash("/"),
    PathSep("::"),
    Colon(":"),
    Percent("%"),
    Not("!"),
    And("&"),
    Or("|"),
    AndAnd("&&"),
    OrOr("||"),
    Comma(",");

    companion object {
        fun fromPunctuation(punctuation: String): Punctuations = when (punctuation) {
            "+" -> Plus
            "-" -> Minus
            "/" -> Slash
            "::" -> PathSep
            ":" -> Colon
            "%" -> Percent
            "!" -> Not
            "&" -> And
            "|" -> Or
            "&&" -> AndAnd
            "||" -> OrOr
            else -> throw UnknownError("Unknown punctuation")
        }
    }
}
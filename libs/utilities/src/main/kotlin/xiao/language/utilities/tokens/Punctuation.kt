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
        private val punctuationsMap = values().associateBy { it.punctuation }

        fun fromPunctuation(punctuation: String): Punctuations = punctuationsMap[punctuation] ?: throw UnknownError()
    }
}
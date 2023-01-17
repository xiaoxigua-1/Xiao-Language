package xiao.language.utilities

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
    OrOr("||");

    companion object {
        val punctuations = Punctuations.values().map { it.punctuation }
    }
}
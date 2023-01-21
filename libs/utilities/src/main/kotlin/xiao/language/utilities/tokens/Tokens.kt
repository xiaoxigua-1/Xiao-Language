package xiao.language.utilities.tokens

sealed class Tokens {
    object Identifier : Tokens()
    data class Keyword(val type: Keywords) : Tokens()
    data class Literal(val type: xiao.language.utilities.tokens.Literal) : Tokens()
    object RawLiteral : Tokens()
    object Number : Tokens()
    data class Punctuation(val punctuation: Punctuations) : Tokens()
    object Whitespace : Tokens()
    object NewLine : Tokens()
    data class Delimiters(val type: xiao.language.utilities.tokens.Delimiters) : Tokens()
    object EOF : Tokens()
}
package xiao.language.utilities

import xiao.language.utilities.tokens.Tokens


data class Token(val type: Tokens, val value: String, val span: Span) {
    constructor(type: Tokens, value: Char, span: Span) : this(type, value.toString(), span)
}

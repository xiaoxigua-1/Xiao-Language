package xiaoLanguage.ast

import xiaoLanguage.tokens.Token

data class Type(val typeTokens: Token, val array: Int) {
    val descriptor: String
        get() = (0..array).joinToString("") { "[" } + when (typeTokens.literal) {
            "Str" -> "Ljava/lang/String"
            "Int" -> "S"
            "Int8" -> "B"
            "Int32" -> "I"
            "Int64" -> "J"
            "F32" -> "F"
            "F64" -> "D"
            "Char" -> "C"
            "Bool" -> "Z"
            "Unit" -> "V"
            "Null" -> ""
            else -> typeTokens.literal
        }
}

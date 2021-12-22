package xiaoLanguage.ast

import xiaoLanguage.tokens.Position
import xiaoLanguage.tokens.Token

data class Class(
    val classKeyword: Token,
    val className: Token,
    val functions: MutableList<Function>,
    override val position: Position? = null
) : Expression()

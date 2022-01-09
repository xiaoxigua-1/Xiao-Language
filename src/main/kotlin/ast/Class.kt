package xiaoLanguage.ast

import xiaoLanguage.tokens.Token

data class Class(
    val classKeyword: Token?,
    val className: Token,
    val functions: List<Function>,
) : ASTNode

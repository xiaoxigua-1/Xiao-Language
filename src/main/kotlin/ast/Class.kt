package xiaoLanguage.ast

import xiaoLanguage.tokens.Token

data class Class(
    val accessorToken: Token? = null,
    val classKeyword: Token,
    val className: Token,
    val functions: MutableList<Function>,
    val accessor: Accessor = Accessor.stringToAccessor(accessorToken?.literal)
)

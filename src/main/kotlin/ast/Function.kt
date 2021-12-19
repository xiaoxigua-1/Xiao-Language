package xiaoLanguage.ast

import xiaoLanguage.tokens.Token

data class Function(
    val accessorToken: Token?,
    val functionKeyword: Token,
    val functionName: Token,
    val parameters: List<Parameter>,
    val returnType: Type?,
    val accessor: Accessor = Accessor.stringToAccessor(accessorToken?.literal)
)

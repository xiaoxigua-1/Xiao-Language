package xiaoLanguage.ast

import xiaoLanguage.tokens.Token

data class Function(
    val accessorToken: Token? = null,
    val functionKeyword: Token,
    val functionName: Token,
    val parameters: List<Parameter>,
    val colon: Token,
    val returnType: Type?,
    val statements: List<Statement>,
    val accessor: Accessor = Accessor.stringToAccessor(accessorToken?.literal)
)

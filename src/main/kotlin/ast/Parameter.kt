package xiaoLanguage.ast

import xiaoLanguage.tokens.Token

data class Parameter(val name: Token, val colon: Token, var type: Type) : ASTNode

package xiaoLanguage.ast

import xiaoLanguage.tokens.Position
import xiaoLanguage.tokens.Token

data class Import(val importKeyword: Token, val path: List<Token>) :
    ASTNode

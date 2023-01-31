package xiao.language.utilities.ast

import xiao.language.utilities.Token

data class Parameter(val name: Token, val colon: Token, val type: Expressions)

data class Parameters(val left: Token, val parameters: List<Parameter>, val right: Token)

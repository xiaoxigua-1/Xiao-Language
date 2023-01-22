package xiao.language.utilities.ast

import xiao.language.utilities.Token

data class Parameter(val name: Token, val colon: Token, val type: Token)

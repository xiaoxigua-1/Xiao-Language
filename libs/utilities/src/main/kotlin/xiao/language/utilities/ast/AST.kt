package xiao.language.utilities.ast

import xiao.language.utilities.Token

sealed class AST {
    data class Function(val kwd: Token, val name: Token): AST()
}
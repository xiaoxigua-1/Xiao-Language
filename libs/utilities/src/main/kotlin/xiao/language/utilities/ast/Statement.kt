package xiao.language.utilities.ast

import xiao.language.utilities.Token

sealed class Statement {
    data class Function(val kwd: Token, val name: Token): Statement()

    data class Expression(val expression: Expression): Statement()
}
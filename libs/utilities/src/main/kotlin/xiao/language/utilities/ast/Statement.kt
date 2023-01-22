package xiao.language.utilities.ast

import xiao.language.utilities.Token

sealed class Statement {
    data class Function(val visibility: Visibility, val kwd: Token, val name: Token, val parameter: List<Parameter>) : Statement()

    data class Variable(val kwd: Token, val name: Token, val equal: Token, val value: Statement) : Statement()

    data class Expression(val expression: Expressions) : Statement()
}
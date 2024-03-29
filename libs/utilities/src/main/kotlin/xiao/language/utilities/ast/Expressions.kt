package xiao.language.utilities.ast

import xiao.language.utilities.Token

sealed class Expressions {
    data class Path(val nodes: List<Expressions>) : Expressions()

    data class Identifier(val name: Token) : Expressions()

    data class Sub(val expression: Expressions) : Expressions()

    data class Call(val name: Token, val args: List<Expressions>) : Expressions()


}

package xiaoLanguage.ast

import xiaoLanguage.tokens.Token

sealed class Operator {
    abstract val operator: Token
    abstract val expressions: List<Expression>

    data class Plus(
        override val operator: Token,
        override val expressions: List<Expression>,
    ) : Operator()
}

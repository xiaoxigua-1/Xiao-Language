package xiaoLanguage.ast

import xiaoLanguage.tokens.Position
import xiaoLanguage.tokens.Token

sealed class Expression : ASTNode {
    abstract val position: Position?

    data class CallFunctionExpression(
        val functionName: Token,
        val args: List<Expression>,
        override val position: Position? = null
    ) : Expression()

    data class OperatorExpression(
        val operator: Operator,
        override val position: Position? = null
    ) : Expression()

    data class VariableExpression(
        val value: Token,
        override val position: Position = value.position
    ) : Expression()

    data class StringExpression(
        val value: Token,
        override val position: Position? = value.position
    ) : Expression()

    data class IntExpression(
        val value: Token,
        override val position: Position? = value.position
    ) : Expression()

    data class FloatExpression(
        val value: Token,
        override val position: Position? = value.position
    ) : Expression()
}

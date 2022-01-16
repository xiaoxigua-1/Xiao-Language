package xiaoLanguage.ast

import xiaoLanguage.tokens.Position
import xiaoLanguage.tokens.Token

sealed class Expression : ASTNode {
    abstract val position: Position?

    data class CallExpression(
        val name: Token,
        val args: List<Expression>,
        override val position: Position? = null
    ) : Expression()

    data class ReSetVariableExpression(
        val variableName: Token,
        val reSetValue: List<Expression>,
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

    data class BoolExpression(
        val value: Token,
        override val position: Position? = value.position
    ) : Expression()

    data class NullExpression(
        val value: Token,
        override val position: Position? = value.position
    ) : Expression()

    data class GeneratorExpression(
        val value: List<Token>,
        override val position: Position? = null
    ) : Expression()
}

package xiaoLanguage.ast

import xiaoLanguage.tokens.Position
import xiaoLanguage.tokens.Token

sealed class Expression : ASTNode {
    abstract val position: Position?

    data class CallFunctionExpression(
        val path: List<Token>,
        val functionName: Token,
        val args: List<Token>,
        override val position: Position? = null
    ) : Expression()

    data class OperatorExpression(
        val operators: Operator,
        override val position: Position? = null
    ) : Expression()

    data class VariableExpression(
        val path: List<Token>,
        val variableName: Token,
        override val position: Position = variableName.position
    ) : Expression()
}

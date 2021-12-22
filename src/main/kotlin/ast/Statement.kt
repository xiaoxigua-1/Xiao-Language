package xiaoLanguage.ast

import xiaoLanguage.tokens.Position
import xiaoLanguage.tokens.Token

sealed class Statement : ASTNode {
    abstract val position: Position?

    data class VariableDeclaration(
        val variableKeyword: Token,
        val variableName: Token,
        val type: Type?,
        val expression: Expression?,
        override val position: Position = variableName.position
    ): Statement()

    data class ExpressionStatement(
        val expression: Expression?,
        override val position: Position? = expression?.position
    ) : Statement()

    data class ReturnStatement(
        val expression: Expression?,
        var returnType: Type? = null,
        override val position: Position? = expression?.position
    ) : Statement()

    data class IfStatement(
        val expression: Expression?,
        val statements: List<Statement>,
        override val position: Position?
    ) : Statement()
}

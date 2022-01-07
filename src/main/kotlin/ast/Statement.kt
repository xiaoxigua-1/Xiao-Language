package xiaoLanguage.ast

import xiaoLanguage.tokens.Position
import xiaoLanguage.tokens.Token

sealed class Statement : ASTNode {
    abstract val position: Position?

    data class VariableDeclaration(
        val variableKeyword: Token,
        val mutKeyword: Token? = null,
        val variableName: Token,
        val colon: Token?,
        val type: Type?,
        val expression: Expression?,
        var findId: Int? = null,
        override val position: Position = variableName.position
    ): Statement()

    data class ExpressionStatement(
        val expression: List<Expression>,
        override val position: Position? = null
    ) : Statement()

    data class ReturnStatement(
        val returnKeyword: Token,
        val expression: Expression?,
        var returnType: Type? = null,
        override val position: Position? = expression?.position
    ) : Statement()

    data class IfStatement(
        val ifKeyword: Token,
        val conditional: Expression?,
        val statements: List<Statement>,
        val elseStatement: List<ElseStatement>,
        override val position: Position? = ifKeyword.position
    ) : Statement()

    data class ElseStatement(
        val elseKeyword: Token,
        val ifKeyword: Token?,
        val conditional: Expression?,
        val statements: List<Statement>
    )

    data class ForStatement(
        val forKeyword: Token,
        val variable: Token,
        val inKeyword: Token,
        val expression: Expression,
        val statements: List<Statement>,
        override val position: Position? = forKeyword.position
    ) : Statement()
}

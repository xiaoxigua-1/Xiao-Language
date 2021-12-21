package xiaoLanguage.ast

import xiaoLanguage.tokens.Position
import xiaoLanguage.tokens.Token

sealed class Statement : ASTNode {
    abstract val position: Position

    data class VariableDeclaration(
        val variableName: Token,
        val type: Type,
        val operator: Token,
        override val position: Position = variableName.position
    ): Statement()
}

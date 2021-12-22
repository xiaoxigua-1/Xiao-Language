package xiaoLanguage.ast

import xiaoLanguage.tokens.Position
import xiaoLanguage.tokens.Token

sealed class Expression : ASTNode {
    abstract val position: Position?;

    data class CallFunctionExpression(
        val path: List<Token>,
        val functionName: Token,
        val parameters: List<Parameter>,
        override val position: Position
    ) : Expression()
}

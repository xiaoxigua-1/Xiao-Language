package xiaoLanguage.ast

import xiaoLanguage.tokens.Position
import xiaoLanguage.tokens.Token

sealed class Expression : ASTNode {
    abstract val position: Position?;

    data class CallFunctionExpression(
        val path: List<Token>,
        val functionName: Token,
        val args: List<Token>,
        override val position: Position? = null
    ) : Expression()

    data class OperatorExpression(
        val operators: List<Expression>,
        override val position: Position? = null
    ) : Expression()

    data class VariableExpression(
        val path: List<Token>,
        val variableName: Token,
        override val position: Position = variableName.position
    ) : Expression()

    data class Type(val typeTokens: Token,val array: Int, override val position: Position? = null) : Expression() {
        val descriptor: String
            get() = (0..array).joinToString("") { "[" } + when (typeTokens.literal) {
                "Str" -> "Ljava/lang/String"
                "Int" -> "S"
                "Int8" -> "B"
                "Int32" -> "I"
                "Int64" -> "J"
                "F32" -> "F"
                "F64" -> "D"
                "Char" -> "C"
                "Bool" -> "Z"
                "Unit" -> "V"
                "Null" -> ""
                else -> typeTokens.literal
            }
    }
}

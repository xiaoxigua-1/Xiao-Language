package xiaoLanguage.ast

import xiaoLanguage.tokens.Token

sealed class Type {
    abstract val typeString: String
    abstract val descriptor: String?

    data class TypeExpression(
        val tokens: List<Token>, override val typeString: String,
        override val descriptor: String? = null
    ) : Type()

    data class IntType(override val typeString: String = "Int", override val descriptor: String? = null) : Type()

    data class FloatType(override val typeString: String = "Float", override val descriptor: String? = null) : Type()

    data class StrType(override val typeString: String = "Str", override val descriptor: String = "Ljava/lang/String") :
        Type()

    data class NullType(override val typeString: String = "Null", override val descriptor: String = "") : Type()

    data class VoidType(override val typeString: String = "Void", override val descriptor: String = "V") : Type()

    data class BoolType(override val typeString: String = "Bool", override val descriptor: String = "B") : Type()

    data class ListType(
        val type: Type,
        override val typeString: String = "List<${type.typeString}>",
        override val descriptor: String = "[${type.descriptor}"
    ) : Type()
}

package xiaoLanguage.ast

import xiaoLanguage.tokens.Token

sealed class Type {
    abstract val typeString: String

    data class TypeExpression(val tokens: List<Token>, override val typeString: String) : Type()

    data class IntType(override val typeString: String = "Int") : Type()

    data class FloatType(override val typeString: String = "Float") : Type()

    data class StrType(override val typeString: String = "Str") : Type()

    data class NullType(override val typeString: String = "Null") : Type()

    data class VoidType(override val typeString: String = "Void") : Type()

    data class BoolType(override val typeString: String = "Bool") : Type()

    data class ListType(val type: Type, override val typeString: String = "List<${type.typeString}>") : Type()
}

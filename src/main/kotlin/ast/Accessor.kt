package xiaoLanguage.ast

import org.objectweb.asm.Opcodes
import xiaoLanguage.tokens.Keyword

enum class Accessor(val access: Int) : ASTNode {
    PublicStatic(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC),
    Public(Opcodes.ACC_PUBLIC),
    Protected(Opcodes.ACC_PROTECTED),
    Private(Opcodes.ACC_PRIVATE);

    companion object {
        fun stringToAccessor(literal: String?): Accessor = when(literal) {
            Keyword.PUBLIC_KEYWORD.keyword -> Public
            Keyword.PROTECTED_KEYWORD.keyword -> Protected
            Keyword.PRIVATE_KEYWORD.keyword -> Private
            else -> PublicStatic
        }
    }
}

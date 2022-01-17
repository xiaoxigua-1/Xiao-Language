package xiaoLanguage.bytecode

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import xiaoLanguage.ast.ASTNode
import xiaoLanguage.ast.Expression
import xiaoLanguage.ast.Function
import xiaoLanguage.ast.Statement
import java.io.File

class Bytecode(val ast: MutableMap<String, MutableList<ASTNode>>, private val outputPath: String) {
    fun toByte() {
        for (clazz in ast) {
            val byte = writeClass(clazz.key, clazz.value, "${clazz.key}.xiao")
            val file = File(outputPath, "${clazz.key}.class")
            file.writeBytes(byte)
        }
    }

    private fun writeClass(className: String, members: MutableList<ASTNode>, source: String): ByteArray {
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        cw.visit(
            61,
            ACC_PUBLIC + ACC_SUPER,
            className,
            null,
            "java/lang/Object",
            null
        )
        cw.visitSource(source, null)

        for (member in members) {
            if (member is Function) {
                writeFunction(cw, member)
            }
        }

        cw.visitEnd()

        return cw.toByteArray()
    }

    private fun writeFunction(cw: ClassWriter, function: Function? = null) {
        var stacks = 1
        var locals = 1
        val mv = cw.visitMethod(
            function?.accessor?.access ?: ACC_PUBLIC,
            function?.functionName?.literal ?: "<init>",
            "",
            null,
            null
        )

        mv.visitCode()

        if (function != null) {
            for (statement in function.statements) {
                when (statement) {
                    is Statement -> writeStatement(mv, statement)
                }
            }
        } else {
            mv.visitVarInsn(ALOAD, 0)
            mv.visitMethodInsn(
                INVOKESPECIAL,
                "java/lang/Object",
                "<init>",
                "()V",
                false
            )
            mv.visitVarInsn(ALOAD, 0)
        }
        mv.visitInsn(RETURN)
        mv.visitMaxs(stacks, locals)
        mv.visitEnd()
    }

    private fun writeStatement(mv: MethodVisitor, statement: Statement) {
        // TODO statements
        when (statement) {
            is Statement.VariableDeclaration -> {
//                writeExpression(mv, statement.expression)
            }
            else -> {}
        }
    }

    private fun writeExpression(mv: MethodVisitor, expression: Expression?) {
        when (expression) {
            is Expression.StringExpression -> {

            }
            else -> {}
        }
    }
}
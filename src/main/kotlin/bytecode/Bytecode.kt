package xiaoLanguage.bytecode

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import xiaoLanguage.ast.ASTNode
import xiaoLanguage.ast.Expression
import xiaoLanguage.ast.Function
import xiaoLanguage.ast.Statement
import kotlin.io.path.Path
import kotlin.io.path.name

class Bytecode(val ast: MutableMap<String, MutableList<ASTNode>>, private val outputPath: String = "./") {
    data class FunctionLocal(var locals: Int = 0, var stacks: Int = 0)

    data class StaticPath(var path: String, val type: String, val name: String)

    private val hierarchy = mutableListOf<MutableList<StaticPath>>(mutableListOf())
    fun toByte() {
        for (clazz in ast) {
            val byte = writeClass(clazz.key, clazz.value, "${clazz.key}.xiao")
//            val file = File(outputPath, "${clazz.key}.class")
//            file.writeBytes(byte)
        }
    }

    private fun writeClass(classPath: String, members: MutableList<ASTNode>, source: String): ByteArray {
        val className = if (hierarchy.size == 1) {
            hierarchy[0] += StaticPath(classPath, "file", Path(classPath).name)
            Path(classPath).name
        } else {
            hierarchy += mutableListOf(StaticPath(classPath, "class", Path(classPath).name))
            hierarchy.joinToString("$") { it.last().name }
        }

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
        val data = FunctionLocal()
        val functionName = if (hierarchy.size == 2 || function == null) {
            function?.functionName?.literal
        } else {
            val path = hierarchy.last().last().path
            val functionNameList = mutableListOf<String>()
            hierarchy += mutableListOf(StaticPath(path, "function", function.functionName.literal))
            hierarchy.forEach { if (it.last().type == "function") functionNameList += it.last().name }
            functionNameList.joinToString("$")
        }

        val mv = cw.visitMethod(
            function?.accessor?.access ?: (ACC_PUBLIC + ACC_STATIC),
            functionName ?: "<init>",
            "()V",
            null,
            null
        )

        mv.visitCode()

        if (function != null) {
            for (statement in function.statements) {
                when (statement) {
                    is Statement -> writeStatement(mv, statement, data)
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
        mv.visitMaxs(data.stacks + 1, data.locals + 1)
        mv.visitEnd()
    }

    private fun writeStatement(mv: MethodVisitor, statement: Statement, data: FunctionLocal) {
        // TODO statements
        when (statement) {
            is Statement.VariableDeclaration -> {
//                writeExpression(mv, statement.expression)
            }
            is Statement.ExpressionStatement -> {
                writeExpressionArray(mv, statement.expression, data)
            }
            else -> {}
        }
    }

    private fun writeExpression(mv: MethodVisitor, expression: Expression?, data: FunctionLocal) {
        when (expression) {
            is Expression.StringExpression -> {
                mv.visitLdcInsn(expression.value.literal)
            }
            is Expression.CallExpression -> {

            }
            else -> {}
        }
    }

    private fun writeExpressionArray(mv: MethodVisitor, expressions: List<Expression>, data: FunctionLocal) {
        for (expression in expressions) {
            writeExpression(mv, expression, data)
        }
    }
}
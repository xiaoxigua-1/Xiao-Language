package xiaoLanguage.bytecode

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import xiaoLanguage.ast.ASTNode
import xiaoLanguage.ast.Expression
import xiaoLanguage.ast.Function
import xiaoLanguage.ast.Statement
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.name

class Bytecode(val ast: MutableMap<String, MutableList<ASTNode>>, val mainFile: File, private val outputPath: String = "./") {
    data class FunctionLocal(var locals: Int = 0, var stacks: Int = 0)

    data class StaticPath(var path: String, val type: String, val name: String)

    private val hierarchy = mutableListOf<MutableList<StaticPath>>(mutableListOf())
    fun toByte() {
        for (clazz in ast) {
            if (clazz.key.startsWith("@std/")) {

            } else {
                val path = if (clazz.key == mainFile.nameWithoutExtension) "Main" else clazz.key
                val byte = writeClass(path, clazz.value, "${clazz.key}.xiao")
                val file = File(outputPath, "${path}.class")
                file.writeBytes(byte)
            }
        }
    }

    private fun writeClass(classPath: String, members: MutableList<ASTNode>, source: String): ByteArray {
        val className = if (hierarchy.size == 1) {
            hierarchy[0] += StaticPath(classPath, "file", Path(classPath).name)
            Path(classPath).name
        } else {
            hierarchy[hierarchy.size - 1] += StaticPath(classPath, "class", Path(classPath).name)
            hierarchy.joinToString("$") { it.last().name }
        }

        hierarchy.add(mutableListOf())

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
        writeFunction(cw)

        for (member in members) {
            if (member is Function) {
                writeFunction(cw, member)
            }
        }

        cw.visitEnd()
        hierarchy.removeLast()
        return cw.toByteArray()
    }

    private fun writeFunction(cw: ClassWriter, function: Function? = null) {
        val data = FunctionLocal()
        if (function != null) {
            val path = hierarchy[hierarchy.size - 2].last().path
            hierarchy[hierarchy.size - 1] += StaticPath(path, "function", function.functionName.literal)
        }

        val functionName = if (hierarchy.size == 2 || function == null) {
            function?.functionName?.literal
        } else {
            val functionNameList = mutableListOf<String>()
            for (index in hierarchy.size downTo 0) {
                if (hierarchy[index].last().type == "function")
                    functionNameList += hierarchy[index].last().name
                else break
            }
            functionNameList.reverse()
            functionNameList.joinToString("$")
        }

        hierarchy.add(mutableListOf())
        val functionDescriptor = if (function != null) {
            data.locals += function.parameters.size
            "(${
                function.parameters.map {
                    it.type.descriptor
                }.joinToString(";")
            };)${function.returnType?.descriptor ?: "V"}"
        } else "()V"

        val mv = cw.visitMethod(
            function?.accessor?.access ?: ACC_PUBLIC,
            functionName ?: "<init>",
            functionDescriptor,
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
            data.locals++
            data.stacks++
        }

        mv.visitInsn(RETURN)
        mv.visitMaxs(data.stacks, data.locals)
        mv.visitEnd()
        hierarchy.removeLast()
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
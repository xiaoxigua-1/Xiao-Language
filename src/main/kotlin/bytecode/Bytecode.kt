package xiaoLanguage.bytecode

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type.*
import xiaoLanguage.ast.Expression
import xiaoLanguage.ast.Function
import java.io.File

class Bytecode(val ast: MutableMap<String, MutableList<Expression>>, private val outputPath: String) {
    fun toByte() {
        for (clazz in ast) {
            val byte = writeClass(clazz.key, clazz.value, "${clazz.key}.xiao")
            val file = File(outputPath, "${clazz.key}.class")
            file.writeBytes(byte)
        }
    }

    private fun writeClass(className: String, members: MutableList<Expression>, source: String): ByteArray {
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

    private fun writeFunction(cw: ClassWriter, function: Function) {
        val mv = cw.visitMethod(
            function.accessor.access,
            function.functionName.literal,
            "(${function.parametersDescriptor})${function.returnTypeDescriptor}",
            null,
            null)
    }
}
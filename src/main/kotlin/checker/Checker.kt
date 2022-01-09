package xiaoLanguage.checker

import xiaoLanguage.ast.ASTNode
import xiaoLanguage.ast.Class
import xiaoLanguage.ast.Function
import xiaoLanguage.ast.Import
import xiaoLanguage.ast.Statement
import xiaoLanguage.compiler.Compiler
import xiaoLanguage.exception.ModuleNotFoundError
import xiaoLanguage.exception.NamingRulesError
import xiaoLanguage.exception.SyntaxError
import xiaoLanguage.util.Report
import java.io.File

class Checker(val ast: MutableList<ASTNode>, private val mainFile: File) {
    private val checkerReport = mutableListOf<Report>()
    private val asts = mutableMapOf<String, MutableList<ASTNode>>()
    private val hierarch = mutableListOf<MutableList<ASTNode>>(mutableListOf())

    fun check(): Pair<MutableMap<String, MutableList<ASTNode>>, List<Report>> {
        val checkAST = mutableListOf<ASTNode>()

        for (node in ast) {
            if (node is Import) {
                checkImport(node)
            } else {
                val expression = checkExpressions(node, hierarch)
                checkAST += expression
                hierarch[0] += expression
            }
        }

        asts[mainFile.nameWithoutExtension] = checkAST

        return asts to checkerReport
    }

    private fun checkExpressions(node: ASTNode, variableHierarchy: MutableList<MutableList<ASTNode>>): ASTNode {
        return when (node) {
            is Statement -> checkStatement(node, variableHierarchy)
            is Function -> checkFunction(node, variableHierarchy)
            is Class -> checkClass(node, variableHierarchy)
            else -> node
        }
    }

    private fun checkStatement(statement: Statement, variableHierarchy: MutableList<MutableList<ASTNode>>): Statement {
        return when (statement) {
            is Statement.VariableDeclaration -> {
                val upHierarchy = variableHierarchy[variableHierarchy.size - 1]
                val upHierarchyVariable = upHierarchy.filterIsInstance<Statement.VariableDeclaration>()

                checkVariable(
                    statement, upHierarchyVariable.size + 1, upHierarchyVariable
                )

                variableHierarchy[variableHierarchy.size - 1] += statement
                statement
            }

            else -> statement
        }
    }

    private fun checkImport(node: Import) {
        val path = when (node.path[0].literal) {
            "xiao" -> {
                ""
            }
            else -> {
                node.path.joinToString("/") { it.literal }
            }
        }
        val file = File("${mainFile.absoluteFile.parent}/$path.xiao")

        if (!file.exists()) checkerReport += Report.Error(
            ModuleNotFoundError("No module named '${node.path.joinToString(".") { it.literal }}'"),
            node.path[0].position,
            node.path[node.path.size - 1].position
        )
        else asts[path] = Compiler(file).compile()[file.nameWithoutExtension]!!
    }

    private fun checkClass(node: Class, variableHierarchy: MutableList<MutableList<ASTNode>>): Class {
        if (node.className.literal[0].isLowerCase()) checkerReport += Report.Warning(
            NamingRulesError("Class naming rules error."),
            node.className.position,
            hint = "correct naming rule 'class Example {}'"
        )
        if (variableHierarchy[variableHierarchy.size - 1].filterIsInstance<Class>()
                .find { it.className.literal == node.className.literal } == null
        ) {
            variableHierarchy[variableHierarchy.size - 1] += node
            variableHierarchy.add(mutableListOf())

            node.functions.map { function ->
                checkExpressions(function, variableHierarchy)
                function
            }

            variableHierarchy.removeAt(variableHierarchy.size - 1)
        } else checkerReport += Report.Error(
            SyntaxError("Identifier '${node.className.literal}' has already been declared"),
            node.className.position,
            node.className.position
        )

        return node
    }

    private fun checkFunction(node: Function, variableHierarchy: MutableList<MutableList<ASTNode>>): Function {
        if (variableHierarchy[variableHierarchy.size - 1].filterIsInstance<Function>()
                .find { it.functionName.literal == node.functionName.literal } == null
        ) {
            variableHierarchy[variableHierarchy.size - 1] += node

            variableHierarchy.add(mutableListOf())
            node.statements.map { statement ->
                checkExpressions(statement, variableHierarchy)
                variableHierarchy[variableHierarchy.size - 1] += statement
                statement
            }

            variableHierarchy.removeAt(variableHierarchy.size - 1)
        } else checkerReport += Report.Error(
            SyntaxError("Identifier '${node.functionName.literal}' has already been declared"),
            node.functionName.position,
            node.functionName.position
        )

        return node
    }

    private fun checkVariable(
        node: Statement.VariableDeclaration, id: Int, variables: List<Statement.VariableDeclaration>
    ): Statement.VariableDeclaration {
        if (variables.find { it.variableName.literal == node.variableName.literal } == null) {
            node.findId = id
        } else checkerReport += Report.Error(
            SyntaxError("Identifier '${node.variableName.literal}' has already been declared"),
            node.position,
            node.position
        )

        return node
    }
}
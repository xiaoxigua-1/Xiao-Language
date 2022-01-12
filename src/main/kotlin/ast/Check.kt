package xiaoLanguage.ast

sealed class Check {
    data class ImportFileValue(val moduleName: String, val value: List<ASTNode>) : ASTNode
    data class ParameterValue(val variableName: String, val type: Type, val id: Int) : ASTNode
}

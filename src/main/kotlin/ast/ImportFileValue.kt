package xiaoLanguage.ast

data class ImportFileValue(val moduleName: String, val value: List<ASTNode>) : ASTNode

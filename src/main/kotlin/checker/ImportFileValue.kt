package xiaoLanguage.checker

import xiaoLanguage.ast.ASTNode

data class ImportFileValue(val moduleName: String, val value: List<ASTNode>) : ASTNode

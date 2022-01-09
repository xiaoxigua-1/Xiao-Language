package xiaoLanguage.checker

import xiaoLanguage.ast.ASTNode
import xiaoLanguage.util.Report

data class CheckReturnData(
    val ast: MutableMap<String, MutableList<ASTNode>>,
    val report: List<Report>,
    val global: List<ASTNode>
)

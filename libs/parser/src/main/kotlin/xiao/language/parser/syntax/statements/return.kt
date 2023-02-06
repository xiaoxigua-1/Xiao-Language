package xiao.language.parser.syntax.statements

import xiao.language.parser.Parser
import xiao.language.parser.syntax.expressions
import xiao.language.utilities.Token
import xiao.language.utilities.ast.Statement

fun Parser.returnStatement(kwd: Token): Statement.Return {
    return Statement.Return(kwd, expressions())
}
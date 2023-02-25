package xiao.language.parser.syntax.statements

import xiao.language.parser.Parser
import xiao.language.parser.statements
import xiao.language.utilities.Token
import xiao.language.utilities.ast.Statement
import xiao.language.utilities.exceptions.Exceptions

fun Parser.public(kwd: Token): Statement.Public {
    val statement = statements()

    if (statement is Statement.Function || statement is Statement.Variable) {
        return Statement.Public(kwd, statement)
    } else throw Exceptions.ExpectException("", kwd.span)
}
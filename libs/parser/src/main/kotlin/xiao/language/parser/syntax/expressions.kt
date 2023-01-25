package xiao.language.parser.syntax

import xiao.language.parser.Parser
import xiao.language.parser.expect
import xiao.language.utilities.ast.Expressions
import xiao.language.utilities.exceptions.Exceptions
import xiao.language.utilities.tokens.Tokens

//fun Parser.expressions(): Expressions {
//
//}

fun Parser.path() {
    val nodes = mutableListOf<Expressions>()
}

fun Parser.identifier(): Expressions {
    val token = expect(Tokens.Identifier, Exceptions.EOFException("Expected identifier."))
    return Expressions.Identifier(token)
}

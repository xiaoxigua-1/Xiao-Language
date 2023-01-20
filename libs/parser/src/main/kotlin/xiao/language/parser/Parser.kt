package xiao.language.parser

import xiao.language.lexer.Lexer

data class Parser(val lexer: Lexer): Iterator<String> {
    override fun hasNext(): Boolean {
        TODO("Not yet implemented")
    }

    override fun next(): String {
        TODO("Not yet implemented")
    }
}

fun Parser.nextExpression() {

}
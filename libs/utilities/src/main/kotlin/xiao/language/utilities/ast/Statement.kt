package xiao.language.utilities.ast

import xiao.language.utilities.Span
import xiao.language.utilities.Token

sealed class Statement {
    abstract val span: Span

    data class Function(
        val kwd: Token,
        val name: Expressions,
        val parameter: Parameters,
        val returnType: Expressions?,
        val block: Expressions,
        override val span: Span = Span(kwd.span.start, block.span.end)
    ) : Statement()

    data class Variable(
        val kwd: Token, val name: Token, val equal: Token, val value: Statement,
        override val span: Span = Span(kwd.span.start, value.span.end)
    ) : Statement()

    data class Return(
        val kwd: Token, val value: Expressions,
        override val span: Span = Span(kwd.span.start, value.span.end)
    ) : Statement()

    data class Expression(val expression: Expressions, override val span: Span = expression.span) : Statement()

    data class Public(
        val kwd: Token,
        val statement: Statement,
        override val span: Span = Span(kwd.span.start, statement.span.end)
    ) : Statement()
}
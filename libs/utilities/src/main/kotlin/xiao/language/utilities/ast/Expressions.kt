package xiao.language.utilities.ast

import xiao.language.utilities.Span
import xiao.language.utilities.Token

sealed class Expressions {
    abstract val span: Span

    data class Path(
        val main: Expressions,
        val sub: Expressions,
        override val span: Span = Span(main.span.start, sub.span.end)
    ) : Expressions()

    data class Identifier(val name: Token, override val span: Span = name.span) : Expressions()

    data class String(val value: Token, override val span: Span = value.span) : Expressions()

    data class Sub(
        val main: Expressions,
        val sub: Expressions,
        override val span: Span = Span(main.span.start, sub.span.end)
    ) : Expressions()

    data class Call(val name: Token, val args: List<Expressions>, override val span: Span) : Expressions()

    data class Block(
        val left: Token,
        val statements: List<Statement>,
        val right: Token,
        override val span: Span = Span(left.span.start, right.span.end)
    ) : Expressions()
}

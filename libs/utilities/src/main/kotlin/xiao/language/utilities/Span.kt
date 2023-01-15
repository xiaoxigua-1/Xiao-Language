package xiao.language.utilities

data class Span(val start: Int, val end: Int) {
    constructor(index: Int) : this(index, index)
}

package xiao.language.utilities.exceptions

import xiao.language.utilities.Span

data class EOFException(override val message: String, val span: Span) : Exception()
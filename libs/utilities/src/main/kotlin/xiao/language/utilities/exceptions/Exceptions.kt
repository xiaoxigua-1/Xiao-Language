package xiao.language.utilities.exceptions

import xiao.language.utilities.Span

sealed class Exceptions: Exception() {
    data class EOFException(override val message: String, val span: Span): Exceptions()

    data class ExpectException(override val message: String, val span: Span): Exception()
}

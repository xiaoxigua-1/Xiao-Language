package xiao.language.utilities.exceptions

import xiao.language.utilities.Span

sealed class Exceptions : Exception() {
    open val span: Span? = null

    data class EOFException(override val message: String) : Exceptions()

    data class NumberFormatException(override val message: String, override val span: Span?) : Exceptions()

    data class UnterminatedException(override val message: String, override val span: Span?) : Exceptions()

    data class ExpectException(override val message: String, override val span: Span) : Exceptions()
}

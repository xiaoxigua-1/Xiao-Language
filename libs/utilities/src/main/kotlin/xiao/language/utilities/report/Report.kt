package xiao.language.utilities.report

import xiao.language.utilities.Span
import xiao.language.utilities.exceptions.Exceptions

data class Report(val message: String, val level: Level, val span: Span?) {
    constructor(exceptions: Exceptions, level: Level) : this("${exceptions::class.java.simpleName}: ${exceptions.message}", level, exceptions.span)
}
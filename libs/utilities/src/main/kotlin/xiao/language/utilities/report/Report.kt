package xiao.language.utilities.report

import xiao.language.utilities.Span

data class Report(val message: String, val level: Level, val span: Span)
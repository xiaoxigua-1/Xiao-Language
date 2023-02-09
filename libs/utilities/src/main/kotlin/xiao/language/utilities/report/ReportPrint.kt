package xiao.language.utilities.report

import java.nio.file.Path

data class ReportPrint(val reports: List<Report>, val source: String, val path: String)

private fun ReportPrint.print(report: Report) {
    var index = 1
    var isStart = false
    source.split('\n').forEachIndexed { i, s ->
        val span = report.span
        if (span.start in index..(index + s.length) && !isStart) {
            isStart = true
        } else if (span.end in index..(index + s.length)) {
            println("${i + 1}| $s")
            isStart = false
        }

        if (isStart) {
            println("${i + 1}| $s")
        }
        index += s.length + 1
    }
}

fun ReportPrint.all() {
    for (report in reports) {
        print(report)
    }
}

fun ReportPrint.debug() {
    for (report in reports) {
        if (report.level == Level.Debug) {
            print(report)
        }
    }
}
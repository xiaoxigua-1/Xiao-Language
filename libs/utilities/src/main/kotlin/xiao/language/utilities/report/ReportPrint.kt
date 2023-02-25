package xiao.language.utilities.report

data class ReportPrint(val reports: List<Report>, val source: String, val path: String)

private fun ReportPrint.print(report: Report) {
    var index = 1
    var isStart = false

    message(report.level, report.message)

    if (report.span != null) {
        source.split('\n').forEachIndexed { i, s ->
            val span = report.span
            if (span.start in index..(index + s.length) && !isStart) {
                isStart = true
            }

            val output = if (span.end in index..(index + s.length) && isStart) {
                isStart = false
                "\u001b[0m${i + 1}| " + s.mapIndexed { lineIndex, c ->
                    if (index + lineIndex == span.start || (span.start !in index..(index + s.length) && lineIndex == 0)) "\u001b[4m$c"
                    else if (index + lineIndex == span.end) "$c\u001b[0m"
                    else "$c"
                }.joinToString("") + '\n'

            } else if (isStart) {
                "\u001b[0m${i + 1}| " + s.mapIndexed { lineIndex, c ->
                    if (index + lineIndex >= span.start) "\u001b[4m$c"
                    else "$c"
                }.joinToString("") + '\n'
            } else ""

            print(output)

            index += s.length + 1
        }
    }
}

fun message(level: Level, message: String) {
    val color = when(level) {
        Level.Debug -> "\u001b[34m$message\u001b[0m"
        Level.Error -> "\u001b[31m$message\u001b[0m"
        Level.Warning -> "\u001b[33m$message\u001b[0m"
    }
    println(color)
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
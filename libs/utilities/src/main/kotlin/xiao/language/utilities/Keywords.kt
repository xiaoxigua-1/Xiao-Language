package xiao.language.utilities

enum class Keywords(val kwd: String) {
    Fn("fn");

    companion object {
        val keywords = Keywords.values().map { it.kwd }
    }
}
package xiao.language.utilities.tokens

enum class Keywords(val kwd: String) {
    Fn("fn"),
    Struct("struct"),
    Mutable("mut"),
    Self("self"),
    Use("use"),
    Pub("pub");

    companion object {
        val keywords = Keywords.values().map { it.kwd }
    }
}
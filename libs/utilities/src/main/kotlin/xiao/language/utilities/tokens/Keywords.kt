package xiao.language.utilities.tokens

enum class Keywords(val kwd: String) {
    Fn("fn"),
    Struct("struct"),
    Mutable("mut"),
    Self("self"),
    Use("use"),
    Pub("pub"),
    For("for"),
    If("if"),
    Return("ret");

    companion object {
        val keywords = Keywords.values().map { it.kwd }
        private val keywordsMap = values().associateBy { it.kwd }

        fun fromKeywords(keyword: String): Keywords =
            keywordsMap[keyword] ?: throw UnknownError("Unknown keyword: $keyword")
    }
}
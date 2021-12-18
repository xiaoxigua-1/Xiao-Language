package xiaoLanguage.exception

data class SyntaxError(override val message: String): Exception(message)

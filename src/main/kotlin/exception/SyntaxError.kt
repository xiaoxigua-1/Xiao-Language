package xiaoLanguage.exception

data class SyntaxError(override val message: String = "invalid syntax"): Exception(message)

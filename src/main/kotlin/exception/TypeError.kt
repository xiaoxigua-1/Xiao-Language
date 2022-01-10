package xiaoLanguage.exception

data class TypeError(override val message: String) : Exception(message)

package xiaoLanguage.exception

data class NameError(override val message: String) : Exception(message)

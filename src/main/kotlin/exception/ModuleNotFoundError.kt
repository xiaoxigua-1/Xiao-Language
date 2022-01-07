package xiaoLanguage.exception

data class ModuleNotFoundError(override val message: String): Exception(message)
package xiao.language.utilities.exceptions

import xiao.language.utilities.Token

data class ExpectException(override val message: String, val expect: String): Exception()

package xiao.language.utilities

import kotlin.test.Test

class UtilitiesTest {
    @Test
    fun charIsSymbolTest() {
        val symbols = ('!'..'/') + (':'..'@') + ('['..'`') + ('{'..'~')

        for (symbol in symbols) {
            assert(symbol.isAsciiSymbol())
        }
    }
}
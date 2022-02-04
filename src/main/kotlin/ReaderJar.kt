package xiaoLanguage

import java.io.File
import java.net.URI
import java.util.zip.ZipFile

class ReaderJar {
    companion object {
        fun readJar(filePath: URI): MutableMap<String, String> {
            val file = ZipFile(File(filePath))
            val entries = file.entries()
            val filesText = mutableMapOf<String, String>()

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (!entry.isDirectory && entry.name.endsWith(".xiao")) {
                    val fileStream = file.getInputStream(entry)
                    filesText[entry.name] = String(fileStream.readAllBytes())
                }
            }

            return filesText
        }
    }
}
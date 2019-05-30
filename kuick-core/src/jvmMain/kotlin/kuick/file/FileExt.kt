package kuick.file

import kotlinx.coroutines.*
import java.io.*
import java.nio.charset.*

suspend fun File.readTextSuspend(charset: Charset = Charsets.UTF_8): String {
    val file = this
    return withContext(Dispatchers.IO) {
        file.readText(charset)
    }
}

suspend fun File.writeTextSuspend(text: String, charset: Charset = Charsets.UTF_8) {
    val file = this
    withContext(Dispatchers.IO) {
        file.writeText(text, charset)
    }
}

suspend fun File.deleteSuspend() {
    val file = this
    withContext(Dispatchers.IO) {
        file.delete()
    }
}

suspend fun File.listFilesSuspend(): Array<File> {
    val file = this
    return withContext(Dispatchers.IO) {
        file.listFiles()
    }
}

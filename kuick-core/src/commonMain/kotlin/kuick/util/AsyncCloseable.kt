package kuick.util

interface AsyncCloseable {
    suspend fun close()
}
fun AsyncCloseable(callback: suspend () -> Unit) = object : AsyncCloseable {
    override suspend fun close() = callback()
}

suspend inline fun <T : AsyncCloseable> T.use(callback: (T) -> Unit) {
    try {
        callback(this)
    } finally {
        this.close()
    }
}
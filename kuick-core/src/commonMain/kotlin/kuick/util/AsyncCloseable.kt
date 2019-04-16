package kuick.util

interface AsyncCloseable {
    suspend fun close()
}
fun AsyncCloseable(callback: suspend () -> Unit) = object : AsyncCloseable {
    override suspend fun close() = callback()
}

suspend inline fun <T : AsyncCloseable> T.use(callback: (T) -> Unit) {
    val result = kotlin.runCatching { callback(this) }
    this.close()
    return result.getOrThrow()
    // Still fails on Kotlin 1.3.30
    /*
    try {
        callback(this)
    } finally {
        this.close()
    }
     */
}
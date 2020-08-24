package kuick.utils

interface AsyncCloseable {
    suspend fun close()
}
fun AsyncCloseable(callback: suspend () -> Unit) = object : AsyncCloseable {
    override suspend fun close() = callback()
}

/*
suspend inline fun <T : AsyncCloseable> T.use(callback: (T) -> Unit) {
    //val result = kotlin.runCatching { callback(this) }
    //this.close()
    //return result.getOrThrow()
    return callback(this)
    // Still fails on Kotlin 1.3.30
    /*
    try {
        callback(this)
    } finally {
        this.close()
    }
     */
}
 */

// @TODO: Bug in Kotlin.JS related to inline
// https://youtrack.jetbrains.com/issue/KT-29120
//inline suspend fun <T : AsyncCloseable, R> T.use(callback: T.() -> R): R { // FAILS
//	try {
//		return callback()
//	} finally {
//		close()
//	}
//}

suspend inline fun <T : AsyncCloseable, TR> T.use(callback: (T) -> TR): TR {
    var error: Throwable? = null
    val result = try {
        callback(this)
    } catch (e: Throwable) {
        error = e
        null
    }
    close()
    if (error != null) throw error
    return result as TR
}

package kuick.caching

import kuick.concurrent.async.*
import kuick.file.*
import kuick.json.*
import kuick.util.*
import kuick.utils.*
import java.io.*
import kotlin.reflect.*

class DiskCache<V : Any>(val clazz: KClass<V>, override val name: String, val cacheDir: File) : Cache<String, V> {
    fun getFileForKey(key: String): File = File(cacheDir, "kuick-cache-$name-${key.toByteArray().sha1().hex}")

    private val asyncThread = NamedAsyncThreads()

    override suspend fun get(key: String, builder: suspend (key: String) -> V): V = asyncThread(key) {
        val file = getFileForKey(key)
        //println("File: $file")
        when {
            file.exists() -> Json.fromJson(file.readTextSuspend(), clazz)
            else -> builder(key).also { file.writeTextSuspend(Json.toJson(it)) }
        }
    }

    override suspend fun invalidate(key: String) = asyncThread(key) {
        getFileForKey(key).deleteSuspend()
        Unit
    }
}

inline fun <reified T : Any> DiskCache(name: String, cacheDir: File = File(System.getProperty("java.io.tmpdir"))) =
        DiskCache(T::class, name, cacheDir)

package kuick.caching

import kuick.concurrent.async.*
import kuick.file.*
import kuick.utils.*
import java.io.*

class DiskCache(
    override val name: String,
    val cacheDir: File = File(System.getProperty("java.io.tmpdir"))
) : Cache<String, String> {
    init {
        cacheDir.mkdirs()
    }

    fun getFileForKey(key: String): File = File(cacheDir, "kuick-cache-$name-${key.toByteArray().sha1().hex}")

    private val asyncGlobal = AsyncThread()
    private val asyncThread = NamedAsyncThreads()

    override suspend fun get(key: String, builder: suspend (key: String) -> String): String = asyncThread(key) {
        asyncGlobal {
            val file = getFileForKey(key)
            //println("File: $file")
            when {
                file.exists() -> file.readTextSuspend()
                else -> builder(key).also { file.writeTextSuspend(it) }
            }
        }
    }

    override suspend fun invalidate(key: String) = asyncThread(key) {
        asyncGlobal {
            getFileForKey(key).deleteSuspend()
            Unit
        }
    }

    override suspend fun invalidateAll() {
        asyncGlobal {
            val base = "kuick-cache-$name-"
            for (file in cacheDir.listFilesSuspend()) {
                if (file.startsWith(base)) {
                    file.deleteSuspend()
                }
            }
        }
    }
}

package kuick.caching
/*
import kotlinx.coroutines.withContext
import kuick.bus.Bus
import kuick.bus.bus
import kuick.bus.publishAsync
import kuick.repositories.ModelQuery
import kuick.repositories.ModelRepository
import kuick.repositories.patterns.ModelChangeType
import kuick.repositories.patterns.changeEventTopic
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KClass



//---------------------------
// Caching repo
open class CachedModelRepositoryDecorator<I: Any, T: Any>(
        private val modelClass: KClass<T>,
        private val repo: ModelRepository<I, T>
): ModelRepository<I, T> {

    override suspend fun init() {
        repo.init()
    }

    override suspend fun insert(t: T): T {
        val t = repo.insert(t)
        publishAsync(cacheName(), t)
        return t
    }

    override suspend fun update(t: T): T {
        val t = repo.update(t)
        publishAsync(cacheName(), t)
        return t
    }
    override suspend fun updateBy(t: T, q: ModelQuery<T>): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun delete(i: I) {
        repo.delete(i)
        publishAsync(cacheName(), i)
    }

    override suspend fun findById(i: I): T? = repo.findById(i)

    override suspend fun findOneBy(q: ModelQuery<T>): T? = repo.findOneBy(q)

    override suspend fun findBy(q: ModelQuery<T>): List<T> = repo.findBy(q)

    private fun cacheName() = "${modelClass.simpleName}-cache"
}



//---------------------------
// Caching context

class CacheContext(val cacheKeys: MutableList<String>? = null) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<CacheContext>
}

suspend fun cacheKeys(): MutableList<String>? = coroutineContext[CacheContext]?.cacheKeys



//---------------------------
// Cache manager


class CacheManager2(val bus: Bus) {

    private val caches = mutableMapOf<String, MutableMap<*, *>>()
    private val listening = mutableSetOf<String>()

    suspend fun <K:Any, T:Any> cached(cacheName: String,
                                      key: K,
                                      compute: suspend (K) -> T,
                                      vararg cachesOn: String): T {

        cacheKeys()?.add(cacheName)

        val myCachesOn = mutableListOf<String>(* cachesOn)

        return withContext(CacheContext(myCachesOn)){

            cacheByName<K, T>(cacheName).getOrPutSuspendable(key) {
                println("Caching: ${cacheName}/${key} based on ${myCachesOn}/${key}")
                val out = compute(key)

                once<K>(cacheName) {
                    myCachesOn.forEach { cachesOn ->
                        bus.registerAsync<K>(cachesOn) {key ->
                            println("Invalidating derivated cache: ${cacheName}/${key} based on ${cachesOn}/${key}")
                            cacheByName<K, Any>(cacheName).remove(key)
                            bus.publishAsync(cacheName, key)
                        }
                    }
                }

                out
            }
        }
    }


    suspend fun <K:Any, T:Any> invalidates(cacheName: String, key: K, action: suspend (K) -> T): T {
        val out = action(key)
        println("Invalidating cache: ${cacheName}/${key}")
        bus.publishAsync(cacheName, key)
        return out
    }


    private fun <K:Any, T:Any> cacheByName(name: String): MutableMap<K, T> =
            caches.getOrPut(name) { mutableMapOf<K, T>() } as MutableMap<K, T>

    private suspend fun <K:Any> once(name: String, action: suspend () -> Unit) {
        if (!listening.contains(name)) {
            listening.add(name)
            action()
        }
    }
}
        */

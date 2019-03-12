package kuick.caching

import kuick.bus.Bus


class CacheManager(val bus: Bus) {

    suspend fun <K:Any, T:Any> cached(cacheName: String, key: K, compute: suspend (K) -> T, vararg cachesOn: String): T {

        once(cacheName) {
            cachesOn.forEach { cachesOn ->
                bus.registerAsync<K>(cachesOn) {
                    println("Invalidating derivated cache: ${cacheName}/${key} based on ${cachesOn}/${key}")
                    cacheByName<K, T>(cacheName).remove(it)
                    bus.publishAsync(cacheName, key)
                }
            }
        }

        return cacheByName<K, T>(cacheName).getOrPutSuspendable(key) {
            println("Caching: ${cacheName}/${key} based on ${cachesOn}/${key}")
            compute(key)
        }
    }

    suspend fun <K:Any, T:Any> invalidates(cacheName: String, key: K, action: suspend (K) -> T): T {
        val out = action(key)
        println("Invalidating cache: ${cacheName}/${key}")
        bus.publishAsync(cacheName, key)
        return out
    }

    //-------------------

    private val caches = mutableMapOf<String, MutableMap<*, *>>()
    private fun <K:Any, T:Any> cacheByName(name: String): MutableMap<K, T> =
            caches.getOrPut(name) { mutableMapOf<K, T>() } as MutableMap<K, T>

    private val listening = mutableSetOf<String>()
    private suspend fun once(name: String, action: suspend () -> Unit) {
        if (!listening.contains(name)) {
            listening.add(name)
            action()
        }
    }

}

suspend fun <K:Any, T:Any> MutableMap<K, T>.getOrPutSuspendable(key: K, calc: suspend (K) -> T): T {
    var out = get(key)
    if (out == null) {
        out = calc(key)
        put(key, out)
    }
    return out
}


package kuick.caching

import kotlin.math.roundToInt


class SimpleCacheService(val caches: CacheManager) {

    suspend fun getCombined(num1: Int, num2: Int): Int =
            caches.cached("getCombined", Pair(num1, num2), {
                getDoubledNumber(num1) + getPow2Number(num2)
            }, "getDoubledNumber", "getPow2Number")

    suspend fun getDoubledNumber(num: Int): Int =
            caches.cached("getDoubledNumber", num,
                    { num * 2 }, "doubled-numbers")

    suspend fun getPow2Number(num: Int): Int =
            caches.cached("getPow2Number", num,
                    { Math.pow(num.toDouble(), 2.0).roundToInt() }, "powered-numbers")

    suspend fun invalidateNumberCache(numcache: String, num: Int) =
            caches.invalidates(numcache, num){}
}
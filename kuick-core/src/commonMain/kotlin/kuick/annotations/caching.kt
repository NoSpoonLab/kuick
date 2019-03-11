package kuick.annotations

@Target(AnnotationTarget.CLASS)
annotation class WithCache(val name: String, vararg val on: String)

fun WithCache.toInfo() = CacheInfo(name, on)


@Target(AnnotationTarget.FUNCTION)
annotation class Cached(val name: String = "", vararg val on: String = emptyArray())

data class CacheInfo(val name: String, val on: Array<out String>) {

    fun withDefault(def: CacheInfo) = copy(
            name = if (name == "") def.name else name,
            on = if (on.isEmpty()) def.on else on
    )

    fun cacheKey() = "\"" + on.map { "${'$'}{$it}" }.joinToString("|") + "\""

}

fun Cached.toInfo() = CacheInfo(name, on)

@Target(AnnotationTarget.FUNCTION)
annotation class InvalidatesCache(val name: String = "", val on: String = "")
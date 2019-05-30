package kuick.caching

fun <K : Any, V> Cache<K, V>.withName(name: String) = object : Cache<K, V> by this {
    override val name: String get() = name
}

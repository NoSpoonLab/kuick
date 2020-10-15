package kuick.event

import kuick.concurrent.Lock
import kuick.utils.fastForEach
import java.io.Closeable

class EventHandler<K, V> {
    private val lock = Lock()
    private val handlers = LinkedHashMap<K, ArrayList<(V) -> Unit>>()

    fun register(key: K, handler: (V) -> Unit): Closeable {
        lock { handlers.getOrPut(key) { arrayListOf() }.add(handler) }
        return Closeable { lock { handlers[key]?.remove(handler) } }
    }

    fun dispatch(key: K, value: V) {
        val handlers = lock { handlers[key]?.toList() }
        handlers?.fastForEach { handler ->
            handler(value)
        }
    }

    fun clear() {
        lock { handlers.clear() }
    }

    fun clear(key: K) {
        lock { handlers.remove(key) }
    }
}

class SuspendEventHandler<K, V> {
    private val lock = Lock()
    private val handlers = LinkedHashMap<K, ArrayList<suspend (V) -> Unit>>()

    fun register(key: K, handler: suspend (V) -> Unit): Closeable {
        lock { handlers.getOrPut(key) { arrayListOf() }.add(handler) }
        return Closeable { lock { handlers[key]?.remove(handler) } }
    }

    suspend fun dispatch(key: K, value: V) {
        val handlers = lock { handlers[key]?.toList() }
        handlers?.fastForEach { handler ->
            handler(value)
        }
    }

    fun clear() {
        lock { handlers.clear() }
    }

    fun clear(key: K) {
        lock { handlers.remove(key) }
    }
}

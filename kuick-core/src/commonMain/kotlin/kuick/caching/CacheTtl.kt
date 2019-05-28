package kuick.caching

import kuick.concurrent.Lock
import kuick.ds.PriorityQueue
import kuick.time.Clock
import kuick.time.SimpleClock
import kuick.time.now

class CacheTtl<K : Any, V : Any>(
        val parent: Cache<K, V>,
        val maxEntries: Int = CacheTtl.UNLIMITED,
        val maxMilliseconds: Int = CacheTtl.UNLIMITED,
        val clock: Clock = SimpleClock,
        val onRemove: (K) -> Unit = {}
) : Cache<K, V> {
    companion object {
        val UNLIMITED = Int.MAX_VALUE
    }

    override val name: String get() = parent.name

    private val lock = Lock()
    private val entriesToTtl = LinkedHashMap<K, Long>()
    private val K.removeTime get() = (entriesToTtl.getOrElse(this) { 0L })
    private val entries = PriorityQueue<K>(Comparator { a, b -> a.removeTime.compareTo(b.removeTime) })

    override suspend fun get(key: K, builder: suspend (key: K) -> V): V {
        return parent.get(key) {
            val value = builder(key)
            val now = clock.now()

            // Remove old items (by time or by entries)
            while (true) {
                val head = lock {
                    if (entries.isNotEmpty() && (now >= entries.head.removeTime) || (entries.size >= maxEntries)) {
                        //println("entries.head.removeTime >= now : ${entries.head.removeTime >= now}")
                        //println("entries.size > maxEntries : ${entries.size > maxEntries}")
                        entries.removeHead()
                    } else {
                        null
                    }
                } ?: break

                onRemove(head)
                invalidate(head)
            }
            lock {
                entries.remove(key) // @TODO: Improve performance?
                entriesToTtl[key] = now + maxMilliseconds
                entries.add(key)
                value
            }
        }
    }

    override suspend fun invalidate(key: K) {
        lock {
            entriesToTtl.remove(key)
            entries.remove(key) // @TODO: Improve performance?
        }
    }

    override suspend fun invalidateAll() {
        lock {
            entries.clear()
            entriesToTtl.clear()
        }
    }

    override suspend fun close() {
        lock {
            entries.clear()
            entriesToTtl.clear()
        }
        parent.close()
    }
}

fun <K : Any, V : Any> Cache<K, V>.withTtl(maxEntries: Int = CacheTtl.UNLIMITED, maxMilliseconds: Int = CacheTtl.UNLIMITED, clock: Clock = SimpleClock, onRemove: (K) -> Unit = {}): CacheTtl<K, V> =
        CacheTtl(this, maxEntries, maxMilliseconds, clock, onRemove)

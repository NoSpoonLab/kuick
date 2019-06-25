package kuick.repositories

import kotlin.reflect.KProperty1

interface ModelRepository<I : Any, T : Any> : ViewRepository<I, T> {
    // Interface
    suspend fun insert(t: T): T
    suspend fun updateBy(t: T, q: ModelQuery<T>): T
    suspend fun deleteBy(q: ModelQuery<T>)

    // Default implementations
    suspend fun delete(i: I) = deleteBy((idField eq i))
    suspend fun update(t: T): T = updateBy(t, idField eq idField.get(t))
    suspend fun upsert(t: T): T {
        try {
            insert(t)
        } catch (e: Throwable) {
            update(t)
        }
        return t
    }
    suspend fun update(set: Map<KProperty1<T, *>, Any> = mapOf(), incr: Map<KProperty1<T, Number>, Number> = mapOf(), where: ModelQuery<T>): Int = TODO()
    suspend fun insertMany(collection: Collection<T>) = collection.forEach { insert(it) }
    suspend fun updateMany(collection: Collection<T>) = collection.forEach { update(it) }
}

suspend fun <I : Any, T : Any> ModelRepository<I, T>.updateBy(q: ModelQuery<T>, updater: (T) -> T) {
    for (it in findBy(q)) update(updater(it))
}

suspend fun <I : Any, T : Any> ModelRepository<I, T>.updateOneBy(q: ModelQuery<T>, updater: (T) -> T): T =
        update(updater(findOneBy(q)!!))

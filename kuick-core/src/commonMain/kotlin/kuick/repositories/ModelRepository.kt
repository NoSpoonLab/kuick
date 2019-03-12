package kuick.repositories


interface ModelRepository<I : Any, T : Any> : ViewRepository<I, T> {

    suspend fun insert(t: T): T

    suspend fun insertMany(collection: Collection<T>) = collection.forEach { insert(it) }

    suspend fun update(t: T): T

    suspend fun updateMany(collection: Collection<T>) = collection.forEach { update(it) }

    suspend fun updateBy(t: T, q: ModelQuery<T>): T

    suspend fun delete(i: I)

    suspend fun deleteBy(q: ModelQuery<T>)

}

suspend fun <I : Any, T : Any> ModelRepository<I, T>.updateBy(q: ModelQuery<T>, updater: (T) -> T) {
    for (it in findBy(q)) update(updater(it))
}

suspend fun <I : Any, T : Any> ModelRepository<I, T>.updateOneBy(q: ModelQuery<T>, updater: (T) -> T): T =
        update(updater(findOneBy(q)!!))
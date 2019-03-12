package kuick.repositories.patterns

import kuick.repositories.*
import kuick.repositories.memory.ModelRepositoryMemory
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface Cache {
    suspend fun <T:Any> get(key: String): T?
    suspend fun <T:Any> put(key: String, cached: T)
    suspend fun remove(key: String)
}

/**
 * [ModelRepository]
 */
class CachedModelRepository<I: Any, T: Any>(
        val modelClass: KClass<T>,
        val idField: KProperty1<T, I>,
        val repo: ModelRepository<I, T>,
        private val cache: Cache,
        private val cacheField: KProperty1<T, *>
): ModelRepositoryDecorator<I, T>(repo) {

    private suspend fun invalidate(t: T) = cache.remove(cacheField(t).toString())

    override suspend fun insert(t: T): T {
        invalidate(t)
        return super.insert(t)
    }

    override suspend fun update(t: T): T {
        invalidate(t)
        return super.update(t)
    }

    override suspend fun delete(i: I) {
        val t = findById(i) ?: throw IllegalArgumentException()
        invalidate(t)
        super.delete(i)
    }

    override suspend fun findBy(q: ModelQuery<T>): List<T> {
        val keyEq = findCacheQuery(q)
        return if (keyEq != null) {
            val key = keyEq.value.toString()
            var subset = cache.get<List<T>>(key)
            if (subset == null) {
                subset = super.findBy(q)
                cache.put(key, subset)
            }
            val subRepo = ModelRepositoryMemory<I,T>(modelClass, idField)
            subset.forEach { subRepo.insert(it) }
            subRepo.findBy(q)
        } else {
            super.findBy(q)
        }
    }

    private fun findCacheQuery(q: ModelQuery<T>): FieldEqs<T, *>? = when {
        q is FieldEqs<T, *> && q.field == cacheField-> q
        q is FilterExpAnd<T> -> findCacheQuery(q.left) ?: findCacheQuery(q.right)
        else -> throw NotImplementedError("Missing hadling of query type: ${q}")
    }

}
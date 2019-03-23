package kuick.caching

import kuick.repositories.FieldEqs
import kuick.repositories.FilterExpAnd
import kuick.repositories.ModelQuery
import kuick.repositories.ModelRepository
import kuick.repositories.memory.ModelRepositoryMemory
import kuick.repositories.patterns.ModelRepositoryDecorator
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class CachedRepository<I: Any, T: Any, C:Any>(
        val modelClass: KClass<T>,
        val idField: KProperty1<T, I>,
        val repo: ModelRepository<I, T>,
        private val cache: Cache<C, List<T>>,
        private val cacheField: KProperty1<T, C>
): ModelRepositoryDecorator<I, T>(repo) {

    private suspend fun invalidate(t: T) = invalidateKey(cacheField(t))

    private suspend fun invalidateKey(key: C) {
        cache.invalidate(key)
    }

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
        super.delete(i)
        invalidate(t)
    }

    override suspend fun deleteBy(q: ModelQuery<T>) {
        val keyEq = findCacheQuery(q)
        if (keyEq != null) {
            val key = keyEq.value as C
            invalidateKey(key)
        } else {
            findBy(q).map { cacheField(it) }.distinct().forEach { invalidateKey(it) }
        }
        super.deleteBy(q)
    }

    override suspend fun findBy(q: ModelQuery<T>): List<T> {
        val keyEq = findCacheQuery(q)
        return if (keyEq != null) {
            val key = keyEq.value as C
            val subset = cache.get(key) {
                super.findBy(FieldEqs(cacheField, key))
            }
            val subRepo = ModelRepositoryMemory(modelClass, idField)
            subset.forEach { subRepo.insert(it) }
            subRepo.findBy(q)
        } else {
            super.findBy(q)
        }
    }

    private fun findCacheQuery(q: ModelQuery<T>): FieldEqs<T, *>? = when {
        q is FieldEqs<T, *> && q.field == cacheField-> q
        q is FilterExpAnd<T> -> findCacheQuery(q.left) ?: findCacheQuery(q.right)
        else -> null
    }

}
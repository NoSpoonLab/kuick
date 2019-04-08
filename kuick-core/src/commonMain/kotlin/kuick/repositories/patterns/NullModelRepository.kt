package kuick.repositories.patterns

import kuick.repositories.ModelQuery
import kuick.repositories.ModelRepository

class NullModelRepository<I: Any, T: Any>(): ModelRepository<I, T> {

    override suspend fun insert(t: T): T = t

    override suspend fun update(t: T): T = t

    override suspend fun updateBy(t: T, q: ModelQuery<T>): T = t

    override suspend fun delete(i: I) {}

    override suspend fun deleteBy(q: ModelQuery<T>) {}
    override suspend fun init() {}

    override suspend fun getAll(): List<T> = emptyList()

    override suspend fun findById(i: I): T? = null

    override suspend fun findBy(q: ModelQuery<T>): List<T> = emptyList()

}
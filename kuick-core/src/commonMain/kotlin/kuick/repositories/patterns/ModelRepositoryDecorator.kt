package kuick.repositories.patterns

import kuick.repositories.ModelQuery
import kuick.repositories.ModelRepository

/**
 * [ModelRepository]
 */
open class ModelRepositoryDecorator<I: Any, T: Any>(private val repo: ModelRepository<I, T>): ModelRepository<I, T> {

    override suspend fun insert(t: T): T = repo.insert(t)

    override suspend fun update(t: T): T = repo.update(t)

    override suspend fun updateBy(t: T, q: ModelQuery<T>): T {
        findBy(q).forEach { update(t) }
        return t
    }

    override suspend fun delete(i: I) = repo.delete(i)

    override suspend fun deleteBy(q: ModelQuery<T>) = repo.deleteBy(q)

    override suspend fun init() = repo.init()

    override suspend fun findById(i: I): T? = repo.findById(i)

    override suspend fun findBy(q: ModelQuery<T>): List<T> = repo.findBy(q)

    override suspend fun getAll(): List<T> = repo.getAll()

}
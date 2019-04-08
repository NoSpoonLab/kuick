package kuick.repositories.patterns

import kuick.repositories.ModelQuery
import kuick.repositories.ModelRepository

/**
 * A [ModelRepository] decorator that implements the CQRS pattern
 * (https://martinfowler.com/bliki/CQRS.html)
 *
 * It takes 2 repositories: main repository and query repository.
 *
 * Every state change is stored in both repositories, but only main
 * repository is considered to be the *real* model storage.
 *
 * Queries are always solved by the query repository,
 * probably based on an implementation optimized for querying.
 */
open class CQRSModelRepositoryDecorator<I : Any, T : Any>(
    private val mainRepo: ModelRepository<I, T>,
    private val queryRepo: ModelRepository<I, T>
) : ModelRepository<I, T> by queryRepo {

    override suspend fun init() {
        mainRepo.init()
        queryRepo.init()
    }

    override suspend fun getAll(): List<T> = queryRepo.getAll()

    override suspend fun insert(t: T): T {
        val t = mainRepo.insert(t)
        queryRepo.insert(t)
        return t
    }

    override suspend fun update(t: T): T {
        val t = mainRepo.update(t)
        queryRepo.update(t)
        return t
    }

    override suspend fun updateBy(t: T, q: ModelQuery<T>): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun delete(i: I) {
        mainRepo.delete(i)
        queryRepo.delete(i)
    }

    override suspend fun deleteBy(q: ModelQuery<T>) {
        mainRepo.deleteBy(q)
        queryRepo.deleteBy(q)
    }

}
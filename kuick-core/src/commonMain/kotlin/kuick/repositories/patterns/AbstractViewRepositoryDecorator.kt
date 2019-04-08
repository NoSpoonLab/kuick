package kuick.repositories.patterns

import kuick.repositories.ModelQuery
import kuick.repositories.ModelRepository
import kuick.repositories.ViewRepository

open class AbstractViewRepositoryDecorator<I: Any, T: Any>(
        val repo: ModelRepository<I, T>
): ViewRepository<I, T> {

    override suspend fun init() {
        repo.init()
    }

    override suspend fun getAll(): List<T> = repo.getAll()

    override suspend fun findById(i: I): T? = repo.findById(i)

    override suspend fun findBy(q: ModelQuery<T>): List<T> = repo.findBy(q)
}
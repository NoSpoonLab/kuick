package kuick.repositories.patterns

import kuick.repositories.ModelQuery
import kuick.repositories.ModelRepository

/**
 * [ModelRepository]
 */
open class ModelRepositoryDecorator<I: Any, T: Any>(private val repo: ModelRepository<I, T>): ModelRepository<I, T> by repo {

    override suspend fun updateBy(t: T, q: ModelQuery<T>): T {
        findBy(q).forEach { update(t) }
        return t
    }

}
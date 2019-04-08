package kuick.repositories.patterns

import kuick.repositories.*

open class AbstractViewRepositoryDecorator<I: Any, T: Any>(
        val repo: ModelRepository<I, T>
): ViewRepository<I, T> by repo

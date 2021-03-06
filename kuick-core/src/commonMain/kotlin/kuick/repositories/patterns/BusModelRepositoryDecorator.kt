package kuick.repositories.patterns

import kuick.bus.Bus
import kuick.repositories.ModelQuery
import kuick.repositories.ModelRepository
import kotlin.reflect.KClass

/**
 *
 */
open class BusModelRepositoryDecorator<I: Any, T: Any>(
        private val modelClass: KClass<T>,
        private val repo: ModelRepository<I, T>,
        private val bus: Bus
): ModelRepository<I, T> by repo {
    override suspend fun insert(t: T): T {
        val t = repo.insert(t)
        bus.publishAsync(modelClass.changeEventTopic(ModelChangeType.INSERT), t)
        return t
    }

    override suspend fun update(t: T): T {
        val t = repo.update(t)
        bus.publishAsync(modelClass.changeEventTopic(ModelChangeType.UPDATE), t)
        return t
    }
    override suspend fun updateBy(t: T, q: ModelQuery<T>): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun delete(i: I) {
        repo.delete(i)
        bus.publishAsync(modelClass.changeEventTopic(ModelChangeType.DELETE), i)
    }

    override suspend fun deleteBy(q: ModelQuery<T>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
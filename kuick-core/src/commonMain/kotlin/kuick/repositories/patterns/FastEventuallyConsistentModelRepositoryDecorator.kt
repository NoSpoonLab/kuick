package kuick.repositories.patterns

import kuick.bus.Bus
import kuick.repositories.ModelRepository
import kotlin.reflect.KClass

/**
 * [ModelRepository] decorator that defers state changes so write ops
 * are *instantaneous*
 */
class FastEventuallyConsistentModelRepositoryDecorator<I: Any, T: Any>(
        private val modelClass: KClass<T>,
        private val repo: ModelRepository<I, T>,
        private val bus: Bus
): BusModelRepositoryDecorator<I, T>(
        modelClass, NullModelRepository(), bus
) {

    override suspend fun init() {
        repo.init()
        bus.registerAsync<T>(modelClass.changeEventTopic(ModelChangeType.INSERT)) { repo.insert(it) }
        bus.registerAsync<T>(modelClass.changeEventTopic(ModelChangeType.UPDATE)) { repo.update(it) }
        bus.registerAsync<I>(modelClass.changeEventTopic(ModelChangeType.DELETE)) { repo.delete(it) }
    }

}
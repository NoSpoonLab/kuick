package kuick.repositories.patterns

import kuick.bus.Bus
import kuick.repositories.ModelQuery
import kuick.repositories.ModelRepository
import kotlin.reflect.KClass

open class ViewRepositoryDecorator<I: Any, T: Any>(
        repo: ModelRepository<I, T>,
        private val bus: Bus,
        val listeners: ViewListeners<T>
): AbstractViewRepositoryDecorator<I, T>(repo) {

    override suspend fun init() {
        repo.init()

        // Register update listeners
        listeners.updaters.forEach { fkl ->

            val foreignModelKClass = fkl.foreignModel

            suspend fun updater(foreignModel: Any) {

                val affectedViews = repo.findBy(fkl.selector(foreignModel))

                // No affected rows... building
                if (affectedViews.isEmpty()) {
                    val newView = fkl.listener(null, foreignModel)
                    repo.insert(newView)
                } else {
                    affectedViews.forEach { affectedView ->
                        val updatedViews = fkl.listener(affectedView, foreignModel)
                        repo.update(updatedViews)
                    }
                }
            }

            bus.registerAsync<Any>(foreignModelKClass.changeEventTopic(ModelChangeType.UPDATE)) { updater(it) }
            bus.registerAsync<Any>(foreignModelKClass.changeEventTopic(ModelChangeType.INSERT)) { updater(it) }

        }
    }

}

data class ViewListeners<T:Any>(
        val builders: List<Builder<T, Any>> = emptyList(),
        val updaters: List<Updater<T, Any>> = emptyList()
) {

    fun <V: Any> updatesOn(
            foreignModel: KClass<V>,
            selector: (V) -> ModelQuery<T>,
            listener: suspend (T?, V) -> T) =
            copy(updaters = updaters + (Updater(foreignModel, selector, listener) as Updater<T, Any>))

}

data class Builder<T: Any, V: Any>(
        val foreignModel: KClass<V>,
        val listener: suspend (V) -> T
)
data class Updater<T: Any, V: Any>(
        val foreignModel: KClass<V>,
        val selector: (V) -> ModelQuery<T>,
        val listener: suspend (T?, V) -> T
)
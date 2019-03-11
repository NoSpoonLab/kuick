package kuick.bus


typealias AsyncListener<T> = suspend (T) -> Unit

class SyncBus: Bus {

    private val listenersMap: MutableMap<String, MutableList<AsyncListener<*>>> = mutableMapOf()

    override suspend fun <T : Any> publishAsync(topicName: String, event: T) {
        eventListeners(topicName).forEach {
            (it as AsyncListener<T>)(event)
        }
    }

    override fun <T : Any> registerAsync(topicName: String, listener: suspend (T) -> Unit) {
        eventListeners(topicName).add(listener)
    }

    private fun eventListeners(topic: String) = listenersMap.getOrPut(topic) { mutableListOf() }

}
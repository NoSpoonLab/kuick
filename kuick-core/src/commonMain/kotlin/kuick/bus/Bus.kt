package kuick.bus

interface Bus {

    fun <T:Any> registerAsync(topicName: String, listener: suspend (T) -> Unit)

    suspend fun <T:Any> publishAsync(topicName: String, event: T)
}
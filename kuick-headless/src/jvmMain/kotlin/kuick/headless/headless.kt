package kuick.headless

import com.amazonaws.services.lambda.runtime.*
import kotlinx.coroutines.*
import kuick.client.db.*
import kuick.client.jdbc.*

// Used to generate the serverless.yml
annotation class Route(val path: String)
annotation class HandlesEvent(val event: String)

abstract class KuickHeadlessHandler<T> : RequestHandler<Map<String, Any?>, T> {
    lateinit var context: Context

    final override fun handleRequest(input: Map<String, Any?>, context: Context): T {
        this.context = context
        return runBlocking {
            val h2 = JdbcDriver.connectMemoryH2()
            withContext(DbClientPool { h2 }) {
                handler()
            }
        }
    }

    abstract suspend fun handler(): T
}

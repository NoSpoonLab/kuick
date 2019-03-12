package kuick.repositories.elasticsearch

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Module
import kuick.db.DomainTransactionService
import kuick.repositories.squash.DomainTransactionServiceSquash
import org.apache.http.HttpHost
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.jetbrains.squash.connection.DatabaseConnection
import org.jetbrains.squash.dialects.h2.H2Connection
import org.junit.After
import kotlin.reflect.KClass

class InfrastructureGuiceModule(
    val indexClient: RestHighLevelClient
) : AbstractModule() {
    override fun configure() {
        val db: DatabaseConnection = H2Connection.createMemoryConnection()
        bind(DatabaseConnection::class.java).toInstance(db)
        bind(RestHighLevelClient::class.java).toInstance(indexClient)
        bind(DomainTransactionService::class.java).to(DomainTransactionServiceSquash::class.java)
        bind(ElasticSearchConfig::class.java).toInstance(ElasticSearchConfig(waitRefresh = true))
    }
}
abstract class AbstractITTest(modules: List<Module>) {

    constructor(vararg modules: AbstractModule) : this(modules.toList())


    val injector = Guice.createInjector(modules)
    val trService = injector.getInstance(DomainTransactionService::class.java)
    fun <T : Any> instance(kClass: KClass<T>): T = injector.getInstance(kClass.java)
}


abstract class AbstractITTestWithES : AbstractITTest(
        listOf(
            InfrastructureGuiceModule(RestHighLevelClient(RestClient.builder(HttpHost("127.0.0.1", 9200)))))
) {

    @After
    fun tearDown() {
        val client: RestHighLevelClient = injector.getInstance(RestHighLevelClient::class.java)
        client.indices()
            .delete(
                DeleteIndexRequest()
                    .indices("_all"),
                RequestOptions.DEFAULT
            )
    }

}

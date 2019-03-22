package kuick.repositories.elasticsearch

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Module
import kuick.db.DomainTransactionService
import kuick.repositories.squash.DomainTransactionServiceSquash
import kuick.utils.*
import org.apache.http.HttpHost
import org.arquillian.cube.docker.impl.client.containerobject.dsl.*
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.jboss.arquillian.junit.*
import org.jetbrains.squash.connection.DatabaseConnection
import org.jetbrains.squash.dialects.h2.H2Connection
import org.junit.After
import org.junit.runner.*
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

@RunWith(Arquillian::class)
abstract class AbstractITTestWithElasticSearch {
    @DockerContainer
    var container = Container.withContainerName("kuick-elasticsearch-test")
            .fromImage("docker.elastic.co/elasticsearch/elasticsearch:6.6.0")
            .withPortBinding(9200)
            .withEnvironment("discovery.type", "single-node")
            .build()


    val injector by lazy {
        Guice.createInjector(listOf(
                InfrastructureGuiceModule(RestHighLevelClient(RestClient.builder(HttpHost(container.ipAddress, container.getBindPort(9200)))))
        ))
    }

    //val trService = injector.getInstance(DomainTransactionService::class.java)
    //fun <T : Any> instance(kClass: KClass<T>): T = injector.getInstance(kClass.java)

    @After
    fun tearDown() {
        injector.get<RestHighLevelClient>().indices().delete(DeleteIndexRequest().indices("_all"), RequestOptions.DEFAULT)
    }

}

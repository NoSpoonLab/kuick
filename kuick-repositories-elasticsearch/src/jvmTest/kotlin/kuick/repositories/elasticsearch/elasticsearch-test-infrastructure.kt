package kuick.repositories.elasticsearch

import com.google.inject.*
import kuick.db.*
import kuick.repositories.elasticsearch.orm.*
import kuick.repositories.squash.*
import kuick.utils.*
import org.apache.http.*
import org.arquillian.cube.containerobject.*
import org.arquillian.cube.docker.impl.client.containerobject.dsl.*
import org.elasticsearch.action.admin.indices.delete.*
import org.elasticsearch.client.*
import org.jboss.arquillian.junit.*
import org.jetbrains.squash.connection.*
import org.jetbrains.squash.dialects.h2.*
import org.junit.*
import org.junit.runner.*

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
            .withConnectionMode(ConnectionMode.START_AND_STOP_AROUND_CLASS)
            .withAwaitStrategy(AwaitBuilder.logAwait("started"))
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

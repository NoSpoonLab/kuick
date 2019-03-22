package kuick.repositories.elasticsearch

import kuick.db.*
import kuick.di.*
import kuick.repositories.elasticsearch.orm.*
import org.apache.http.*
import org.arquillian.cube.containerobject.*
import org.arquillian.cube.docker.impl.client.containerobject.dsl.*
import org.elasticsearch.action.admin.indices.delete.*
import org.elasticsearch.client.*
import org.jboss.arquillian.junit.*
import org.jetbrains.squash.dialects.h2.*
import org.junit.*
import org.junit.runner.*

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
        Guice {
            bind(RestHighLevelClient(RestClient.builder(HttpHost(container.ipAddress, container.getBindPort(9200)))))
            bindDatabaseSquash(H2Connection.createMemoryConnection())
            bind(ElasticSearchConfig(waitRefresh = true))
        }
    }

    data class TestModel(
            val id: String,
            val val1: String,
            val val2: Int?,
            val val3: Boolean
    )

    protected val modelRepositoryElasticSearch by lazy {
        ModelRepositoryElasticSearch(
                TestModel::class,
                TestModel::id,
                injector.getInstance(IndexClient::class.java),
                {
                    TestModel::val1 to field(TestModel::val1.name, ElasticSearchFieldType.TEXT)
                }
        )
    }


    //val trService = injector.getInstance(DomainTransactionService::class.java)
    //fun <T : Any> instance(kClass: KClass<T>): T = injector.getInstance(kClass.java)

    @After
    fun tearDown() {
        injector.get<RestHighLevelClient>().indices().delete(DeleteIndexRequest().indices("_all"), RequestOptions.DEFAULT)
    }

}

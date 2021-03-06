package kuick.repositories.elasticsearch

import com.google.inject.*
import kuick.di.*
import kuick.json.*
import kuick.repositories.*
import org.elasticsearch.action.*
import org.elasticsearch.action.admin.indices.create.*
import org.elasticsearch.action.admin.indices.get.*
import org.elasticsearch.action.bulk.*
import org.elasticsearch.action.delete.*
import org.elasticsearch.action.get.*
import org.elasticsearch.action.index.*
import org.elasticsearch.action.search.*
import org.elasticsearch.action.support.*
import org.elasticsearch.client.*
import org.elasticsearch.common.xcontent.*
import org.elasticsearch.index.query.*
import org.elasticsearch.search.builder.*
import org.elasticsearch.search.sort.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.*

data class ElasticSearchConfig(
        val waitRefresh: Boolean = false
)

@Singleton
open class IndexClient @Inject constructor(
        private val client: RestHighLevelClient,
        private val injector: Injector
) {

    val config = injector.getOrNull<ElasticSearchConfig>()
    val waitRefresh: Boolean get() = config?.waitRefresh ?: false

    companion object {
        private const val DOCUMENT_TYPE = "doc"
    }

    fun indexExists(indexName: String): Boolean {
        val request = GetIndexRequest()
        request.indices(indexName)
        return client.indices()
                .exists(request, RequestOptions.DEFAULT)
    }

    suspend fun createIndex(indexName: String, mapping: Map<String, Any> = emptyMap()) = suspendCoroutine<Unit> { c ->
        client.indices()
                .createAsync(
                        CreateIndexRequest(indexName).mapping(
                                DOCUMENT_TYPE, mapOf(DOCUMENT_TYPE to mapping)
                        ),
                        RequestOptions.DEFAULT,
                        actionListener(
                                { c.resume(Unit) },
                                { c.resumeWithException(it) }
                        )
                )
    }

    suspend fun get(indexName: String, id: String): GetResponse = suspendCoroutine { c ->
        val getRequest = GetRequest(indexName, DOCUMENT_TYPE, id)
        client.getAsync(getRequest, RequestOptions.DEFAULT, actionListener(
                { c.resume(it) },
                { c.resumeWithException(it) }
        ))

    }

    suspend fun search(
            indexName: String,
            query: QueryBuilder,
            from: Int? = null,
            size: Int? = null,
            sort: OrderByDescriptor<*>? = null
    ): SearchResponse = suspendCoroutine { c ->
        val searchSourceBuilder = SearchSourceBuilder()
        searchSourceBuilder.query(query)
        if (from != null) searchSourceBuilder.from(from)
        if (size != null) searchSourceBuilder.size(size)
        val sortList = sort?.list
        if (sortList != null) {
            for (it in sortList) {
                searchSourceBuilder.sort(it.prop.name, if (it.ascending) SortOrder.ASC else SortOrder.DESC)
            }
        }
        val searchRequest = SearchRequest()
        searchRequest.source(searchSourceBuilder)
        searchRequest.indices(indexName)
        searchRequest.types(DOCUMENT_TYPE)

        client.searchAsync(searchRequest, RequestOptions.DEFAULT, actionListener(
                { c.resume(it) },
                { c.resumeWithException(it) }
        ))
    }


    suspend fun <T : Any> add(indexName: String, id: String, item: T): T = suspendCoroutine { c ->
        val request = IndexRequest(indexName, DOCUMENT_TYPE, id)
                .source(Json.toJson(item), XContentType.JSON).apply {
                    if (waitRefresh) refreshPolicy = WriteRequest.RefreshPolicy.IMMEDIATE
                }
        client.indexAsync(request, RequestOptions.DEFAULT, actionListener(
                { c.resume(item) },
                { c.resumeWithException(it) }
        ))
    }

    suspend fun <T : Any> addMany(indexName: String, idFunction: (T) -> String, items: Collection<T>) = suspendCoroutine<Unit> { c ->
        val bulkRequest = createBulkRequest()
        items.forEach {
            bulkRequest.add(
                    IndexRequest(indexName, DOCUMENT_TYPE, idFunction(it))
                            .source(Json.toJson(it), XContentType.JSON)
            )
        }
        client.bulkAsync(bulkRequest, RequestOptions.DEFAULT, actionListener(
                { c.resume(Unit) },
                { c.resumeWithException(it) }
        ))
    }

    suspend fun delete(indexName: String, id: String) = suspendCoroutine<Unit> { c ->
        val deleteRequest = DeleteRequest(indexName, DOCUMENT_TYPE, id).apply {
            if (waitRefresh) refreshPolicy = WriteRequest.RefreshPolicy.IMMEDIATE
        }
        client.deleteAsync(deleteRequest, RequestOptions.DEFAULT, actionListener(
                { c.resume(Unit) },
                { c.resumeWithException(it) }
        ))
    }

    suspend fun deleteBy(indexName: String, query: QueryBuilder): List<String> {
        val search = this.search(indexName, query)
        return if (search.hits.totalHits > 0) {
            suspendCoroutine { c ->
                val bulkRequest = createBulkRequest()
                search.hits.forEach {
                    bulkRequest.add(DeleteRequest(indexName, DOCUMENT_TYPE, it.id))
                }
                client.bulkAsync(bulkRequest, RequestOptions.DEFAULT, actionListener(
                        { response -> c.resume(response.items.map { it.id }) },
                        { c.resumeWithException(it) }
                ))
            }
        } else {
            emptyList()
        }
    }

    private fun createBulkRequest() = BulkRequest().apply {
        if (waitRefresh) refreshPolicy = WriteRequest.RefreshPolicy.IMMEDIATE
    }

    private fun <T : ActionResponse> actionListener(onResponse: (T) -> Unit, onFailure: (Exception) -> Unit): ActionListener<T> =
            object : ActionListener<T> {
                override fun onResponse(response: T?) = onResponse(response!!)
                override fun onFailure(e: Exception?) = onFailure(e!!)
            }

}
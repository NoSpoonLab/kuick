package kuick.repositories.elasticsearch


import kuick.json.Json
import kuick.repositories.*
import kuick.repositories.elasticsearch.orm.ElasticSearchIndexSchema
import kuick.repositories.elasticsearch.orm.toElasticSearch
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.index.query.QueryBuilders.boolQuery
import org.elasticsearch.index.query.QueryBuilders.matchAllQuery
import org.elasticsearch.search.SearchHit
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class ModelRepositoryElasticSearch<I : String, T : Any>(
    private val modelClass: KClass<T>,
    override val idField: KProperty1<T, I>,
    private val client: IndexClient,
    customMapping: ElasticSearchIndexSchema<T>.() -> Unit = {}
) : ModelRepository<I, T>, ScoredViewRepository<I, T> {
    private val indexName = modelClass.simpleName!!.toLowerCase()
    private val schema = ElasticSearchIndexSchema.fromClass(modelClass, customMapping)

    fun indexExists(): Boolean =
        client.indexExists(indexName)

    override suspend fun init() {
        client.createIndex(indexName, schema.getMapping())
    }

    override suspend fun findBy(q: ModelQuery<T>): List<T> {
        val a = q.tryGetAttributed()
        return client.search(
            indexName,
            q.toElasticSearch(schema),
                a?.skip?.toInt(),
                a?.limit
        ).hits.map {
            it.toModel()
        }.toList()
    }

    override suspend fun searchBy(q: ModelQuery<T>): List<ScoredModel<T>> =
        client.search(
            indexName,
            q.toElasticSearch(schema)
        ).hits.map {
            it.toScoredModel()
        }.toList()

    override suspend fun getAll(): List<T> =
        client.search(
            indexName,
            boolQuery().must(matchAllQuery())
        ).hits.map {
            it.toModel()
        }.toList()


    override suspend fun insert(t: T): T =
        client.add(indexName, idField.get(t), t)

    override suspend fun insertMany(collection: Collection<T>) =
        client.addMany(indexName, { idField.get(it) }, collection)

    override suspend fun update(t: T): T =
        insert(t)

    override suspend fun updateMany(collection: Collection<T>) =
        insertMany(collection)

    override suspend fun updateBy(t: T, q: ModelQuery<T>): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun delete(i: I) =
        client.delete(indexName, i)

    override suspend fun deleteBy(q: ModelQuery<T>) {
        client.deleteBy(indexName, q.toElasticSearch(schema))
    }

    private fun GetResponse.toModel() = Json.fromJson(this.sourceAsString, modelClass)
    private fun SearchHit.toModel() = Json.fromJson(this.sourceAsString, modelClass)
    private fun SearchHit.toScoredModel() = ScoredModel(
        score = this.score,
        model = this.toModel()
    )
}



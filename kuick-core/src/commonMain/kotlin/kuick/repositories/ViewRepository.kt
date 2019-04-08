package kuick.repositories


interface ViewRepository<I : Any, T : Any> {

    suspend fun init()

    suspend fun findById(i: I): T?

    suspend fun findOneBy(q: ModelQuery<T>): T?

    suspend fun findBy(q: ModelQuery<T>): List<T>

    suspend fun getAll(): List<T>

}

suspend fun <I : Any, T : Any> ViewRepository<I, T>.findBy(q: ModelQuery<T>, skip: Long = 0L, limit: Int? = null, orderBy: OrderByDescriptor<T>? = null): List<T> {
    return findBy(AttributedModelQuery(base = q, skip = skip, limit = limit, orderBy = orderBy))
}


data class ScoredModel<T : Any>(
    val score: Float,
    val model: T
)

interface ScoredViewRepository<I : Any, T : Any> {
    suspend fun searchBy(q: ModelQuery<T>): List<ScoredModel<T>>
}

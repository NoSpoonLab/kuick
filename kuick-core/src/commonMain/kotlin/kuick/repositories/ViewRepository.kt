package kuick.repositories


interface ViewRepository<I : Any, T : Any> {

    suspend fun init()

    suspend fun findById(i: I): T?

    suspend fun findOneBy(q: ModelQuery<T>): T?

    suspend fun findBy(q: ModelQuery<T>): List<T>

    suspend fun getAll(): List<T>

}

data class ScoredModel<T : Any>(
    val score: Float,
    val model: T
)

interface ScoredViewRepository<I : Any, T : Any> {
    suspend fun searchBy(q: ModelQuery<T>): List<ScoredModel<T>>
}

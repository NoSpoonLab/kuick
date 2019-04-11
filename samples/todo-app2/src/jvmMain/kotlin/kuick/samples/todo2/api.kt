package kuick.samples.todo2


interface TodoApi {
    suspend fun getOne(
            id: String,
            `$fields`: Set<String>? = null
    ): TodoResult

    suspend fun getAll(): List<TodoResult>

    suspend fun add(text: String): TodoResult
    suspend fun remove(id: String)
}

data class TodoResult(
        val id: String,
        val text: String
)

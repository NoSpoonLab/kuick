package kuick.samples.todo2


interface TodoApi {
    suspend fun getOne(id: String): TodoResult

    suspend fun getAll(): List<TodoResult>

    suspend fun add(text: String, owner: String): TodoResult
    suspend fun remove(id: String)
}

data class TodoResult(
        val id: String,
        val text: String,
        val owner: String
)

interface UserApi {
    suspend fun getOne(id: String): UserResult
    suspend fun add(text: String): UserResult
}

data class UserResult(
        val id: String,
        val name: String
)

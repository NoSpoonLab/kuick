package kuick.samples.todo2

import com.google.inject.Inject
import kuick.models.AbstractId
import kuick.repositories.annotations.MaxLength
import kuick.utils.randomUUID


data class Todo(
        val id: Id,
        @MaxLength(512) val text: String,
        val owner: User.Id
) {

    class Id(id: String = randomUUID()) : AbstractId(id)

}

data class User(
        val id: Id,
        val name: String
) {

    class Id(id: String = randomUUID()) : AbstractId(id)

}

class TodoService
@Inject constructor(
        private val todoRepository: TodoRepository
) {
    suspend fun getOne(id: Todo.Id): Todo? = todoRepository.findById(id)
    suspend fun getAll(): List<Todo> = todoRepository.getAll()
    suspend fun add(text: String, owner: User.Id): Todo = todoRepository.insert(
            Todo(Todo.Id(), text, owner)
    )

    suspend fun remove(id: Todo.Id) = todoRepository.delete(id)

}

class UserService
@Inject constructor(
        private val userRepository: UserRepository
) {
    suspend fun getOne(id: User.Id): User?  = userRepository.findById(id)

    suspend fun add(text: String): User = userRepository.insert(
            User(User.Id(), text)
    )

}

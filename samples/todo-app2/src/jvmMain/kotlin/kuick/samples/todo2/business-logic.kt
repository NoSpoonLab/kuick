package kuick.samples.todo2

import com.google.inject.Inject
import kuick.models.AbstractId
import kuick.repositories.annotations.MaxLength
import kuick.utils.randomUUID


class TodoService
@Inject constructor(
        private val todoRepository: TodoRepository
) {
    suspend fun getOne(id: Todo.Id): Todo? = todoRepository.findById(id)
    suspend fun getAll(): List<Todo> = todoRepository.getAll()
    suspend fun add(text: String): Todo = todoRepository.insert(
            Todo(Todo.Id(), text)
    )

    suspend fun remove(id: Todo.Id) = todoRepository.delete(id)

}


data class Todo(
        val id: Id,
        @MaxLength(512) val text: String
) {

    class Id(id: String = randomUUID()) : AbstractId(id)

}

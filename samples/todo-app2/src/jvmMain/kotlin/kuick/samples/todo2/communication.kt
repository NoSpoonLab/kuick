package kuick.samples.todo2

import com.google.inject.Inject


class TodoController
@Inject constructor(
        private val todoService: TodoService
) : TodoApi {
    override suspend fun getAll(): List<TodoResult> = todoService.getAll().map {
        TodoResult(
                id = it.id.id,
                text = it.text
        )
    }

    override suspend fun add(text: String): TodoResult = todoService.add(text).let {
        TodoResult(
                id = it.id.id,
                text = it.text
        )
    }

    override suspend fun remove(id: String) = todoService.remove(Todo.Id(id))
}

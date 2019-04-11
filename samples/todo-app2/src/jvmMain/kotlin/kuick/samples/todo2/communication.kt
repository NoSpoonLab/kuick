package kuick.samples.todo2

import com.google.inject.Inject


class TodoController
@Inject constructor(
        private val todoService: TodoService
) : TodoApi {

    override suspend fun getOne(id: String): TodoResult = todoService.getOne(Todo.Id(id))?.fromModel()
            ?: throw RuntimeException("404")

    override suspend fun getAll(): List<TodoResult> = todoService.getAll().map {
        it.fromModel()
    }

    override suspend fun add(text: String, owner: String): TodoResult =
            todoService.add(text, User.Id(owner)).fromModel()

    override suspend fun remove(id: String) = todoService.remove(Todo.Id(id))


    private fun Todo.fromModel(): TodoResult =
            TodoResult(
                    id = id.id,
                    text = text,
                    owner = owner.id
            )
}

class UserController
@Inject constructor(
        private val userService: UserService
) : UserApi {
    override suspend fun getOne(id: String): UserResult = userService.getOne(User.Id(id))?.fromModel()
            ?: throw RuntimeException("404")

    override suspend fun add(text: String): UserResult = userService.add(text).fromModel()

    private fun User.fromModel(): UserResult =
            UserResult(
                    id = id.id,
                    name = name
            )

}

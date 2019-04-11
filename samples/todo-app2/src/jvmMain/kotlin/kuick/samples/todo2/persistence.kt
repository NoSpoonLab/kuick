package kuick.samples.todo2

import kuick.repositories.ModelRepository


interface TodoRepository : ModelRepository<Todo.Id, Todo>

interface UserRepository : ModelRepository<User.Id, User>

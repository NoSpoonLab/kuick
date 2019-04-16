package kuick.samples.todo2

import com.google.inject.Binder
import com.google.inject.Singleton
import kuick.client.repositories.DbModelRepository
import kuick.di.bind
import kuick.repositories.patterns.CachedModelRepository
import kuick.repositories.patterns.MemoryCache


fun Binder.configure() {
    //        Version 1
//        bind<TodoRepository>(DbModelRepository(Todo::id).cached(MemoryCache()))
//
//        Version 2
//        @Singleton
//        class TodoRepositoryImpl : TodoRepository,
//                DbModelRepository<Todo.Id, Todo>(Todo::class, Todo::id)
//        bind<TodoRepository>(TodoRepositoryImpl().cached(MemoryCache()))


    //        Version 3
    @Singleton
    class TodoRepositoryImpl : TodoRepository,
            CachedModelRepository<Todo.Id, Todo>( // Had to make it open
                    Todo::class,
                    Todo::id,
                    DbModelRepository(Todo::id),
                    MemoryCache(),
                    Todo::id
            )

    bind<TodoRepository, TodoRepositoryImpl>()

    bind<TodoApi, TodoController>()

    @Singleton
    class UserRepositoryImpl : UserRepository, DbModelRepository<User.Id, User>(User::class, User::id)
    bind<UserRepository, UserRepositoryImpl>()

    bind<UserApi, UserController>()
}

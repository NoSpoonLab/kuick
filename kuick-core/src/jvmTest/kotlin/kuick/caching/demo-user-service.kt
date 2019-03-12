package kuick.caching

import kuick.repositories.ModelRepository
import kuick.repositories.and
import kuick.repositories.eq
import kuick.repositories.gte
import kuick.repositories.memory.ModelRepositoryMemory


data class DemoUser(val appId: String, val userId: String, val name: String, val age: Int)

class DemoUserService(val caches: CacheManager, val repo: DemoUserRepository) {

    suspend fun all() = repo.findBy(DemoUser::appId gte "")

    suspend fun getAllAppUsers(app: String): List<DemoUser> = caches.cached("getAllAppUsers", app, {
        repo.findBy(DemoUser::appId eq app)
    }, "demousers")

    suspend fun getAdultAppUsers(app: String): List<DemoUser> = caches.cached("getAdultAppUsers", app, {
        getAllAppUsers(app).filter { it.age >= 18 }
    }, "getAllAppUsers")

    suspend fun createUser(appId: String, userId: String, name: String, age: Int): DemoUser = caches.invalidates("demousers", appId) {
        val user = DemoUser(appId, userId, name, age)
        repo.insert(user)
    }

    suspend fun updateUserName(appId: String, userId: String, name: String): DemoUser = caches.invalidates("demousers", appId)  {
        val user = repo.findOneBy((DemoUser::appId eq appId) and (DemoUser::userId eq userId))!!
        repo.update(user.copy(name = name))
    }

}

interface DemoUserRepository: ModelRepository<String, DemoUser>

class DemoUserRepositoryImpl: DemoUserRepository,
        ModelRepositoryMemory<String, DemoUser>(DemoUser::class, DemoUser::userId)

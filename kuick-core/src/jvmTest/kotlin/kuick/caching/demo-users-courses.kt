package kuick.caching

import kuick.repositories.eq
import kuick.repositories.memory.ModelRepositoryMemory
import java.util.*


data class User(val userId: String, val name: String)
data class Course(val courseId: String, val name: String)
data class Inscription(val inscriptionId: String, val userId: String, val courseId: String, val inscribedAt: Long)

data class DetailedInscription(val inscriptionId: String,
                               val userId: String, val userName: String,
                               val courseId: String, val courseName: String,
                               val inscribedAt: Long)

class UserService {

    private val repo = ModelRepositoryMemory(User::class, User::userId)

    suspend fun create(name: String): User = repo.insert(User(randomId(), name))
    suspend fun update(id: String, name: String): User {
        val user = repo.findById(id)!!
        return repo.update(user.copy(name = name))
    }
}

class CourseService {

    private val repo = ModelRepositoryMemory(Course::class, Course::courseId)

    suspend fun create(name: String): Course = repo.insert(Course(randomId(), name))
    suspend fun update(id: String, name: String): Course {
        val course = repo.findById(id)!!
        return repo.update(course.copy(name = name))
    }
}

class InscriptionService {

    private val repo = ModelRepositoryMemory(Inscription::class, Inscription::inscriptionId)

    suspend fun inscribeUser(userId: String, courseId: String): Inscription {
        val inscription = Inscription(randomId(), userId, courseId, System.currentTimeMillis())
        return repo.insert(inscription)
    }

    suspend fun getUserInscriptions(userId: String): List<Inscription> =
            repo.findBy(Inscription::userId eq userId)


    suspend fun getCourseInscriptions(courseId: String): List<Inscription> =
            repo.findBy(Inscription::courseId eq courseId)

}

class DetailedInscriptionService {

    private val repo = ModelRepositoryMemory(DetailedInscription::class, DetailedInscription::inscriptionId)

    suspend fun getUserInscriptions(userId: String): List<DetailedInscription> =
            repo.findBy(DetailedInscription::userId eq userId)


    suspend fun getCourseInscriptions(courseId: String): List<DetailedInscription> =
            repo.findBy(DetailedInscription::courseId eq courseId)

}

private fun randomId() = UUID.randomUUID().toString()
package kuick.repositories.patterns

import kotlinx.coroutines.*
import kuick.bus.*
import kuick.repositories.*
import kuick.repositories.memory.*
import kotlin.test.*


class ViewRepositoryDecoratorTests {

    data class User(val userId: String, val name: String)
    data class Course(val courseId: String, val title: String)
    data class Inscription(val inscriptionId: String,
                           val userId: String,
                           val courseId: String,
                           val studyHours: Int)

    // This is a 1-1 mapping from [Inscription], with additional information
    data class InscriptionStatsView(
            val inscriptionId: String,
            val userId: String, val userName: String,
            val courseId: String, val courseTitle: String,
            val studyHours: Int
    ) {

        fun updateWithInscription(inscription: Inscription) = copy(studyHours = inscription.studyHours)
        fun updateWithUser(user: User) = copy(userName = user.name)

        companion object {
            fun build(i: Inscription, u: User, c: Course) =
                    InscriptionStatsView(i.inscriptionId, i.userId, u.name, i.courseId, c.title, i.studyHours)

        }
    }


    val bus = SyncBus()

    val userRepo: ModelRepository<String, User> =
            BusModelRepositoryDecorator(User::class, ModelRepositoryMemory(User::class, User::userId), bus)

    val courseRepo: ModelRepository<String, Course> =
            BusModelRepositoryDecorator(Course::class, ModelRepositoryMemory(Course::class, Course::courseId), bus)

    val inscriptionRepo: ModelRepository<String, Inscription> =
            BusModelRepositoryDecorator(Inscription::class, ModelRepositoryMemory(Inscription::class, Inscription::inscriptionId), bus)

    val inscriptionStatsView = ViewRepositoryDecorator(
            // Undelying repository could be any valid repository: squash, elasticsearch, etc.
            BusModelRepositoryDecorator(InscriptionStatsView::class, ModelRepositoryMemory(InscriptionStatsView::class, InscriptionStatsView::inscriptionId), bus),
            bus,
            ViewListeners<InscriptionStatsView>()
                    .updatesOn(Inscription::class, { InscriptionStatsView::inscriptionId eq it.inscriptionId }) { isv, inscription ->
                        if (isv == null) InscriptionStatsView.build(inscription, userRepo.findById(inscription.userId)!!, courseRepo.findById(inscription.courseId)!!)
                        else isv.updateWithInscription(inscription)
                    }
                    .updatesOn(User::class, { InscriptionStatsView::userId eq it.userId }) { isv, user ->
                        isv?.updateWithUser(user)
                    }
    )


    private suspend fun initRepos() {
        userRepo.init()
        courseRepo.init()
        inscriptionRepo.init()
        inscriptionStatsView.init()
    }

    private suspend fun createMikeAndTestCourse() {
        userRepo.insert(User("mike", "Mike"))
        courseRepo.insert(Course("test_course", "Test course"))
        inscriptionRepo.insert(Inscription("insc_1", "mike", "test_course", 0))
    }

    @Test
    fun `Views should be created whenever viewed models are created`() = runBlocking {
        initRepos()

        createMikeAndTestCourse()

        val isw = inscriptionStatsView.findOneBy(InscriptionStatsView::inscriptionId eq "insc_1")
        assertEquals(InscriptionStatsView("insc_1",
                "mike", "Mike",
                "test_course", "Test course", 0),
                isw)
        Unit
    }


    @Test
    fun `Views should be updated whenever a primary model is updated`() = runBlocking {
        initRepos()
        createMikeAndTestCourse()

        val insc = inscriptionRepo.findById("insc_1")!!

        inscriptionRepo.update(insc.copy(studyHours = 20))

        val isw = inscriptionStatsView.findOneBy(InscriptionStatsView::inscriptionId eq "insc_1")
        assertEquals(InscriptionStatsView("insc_1",
                "mike", "Mike",
                "test_course", "Test course", 20),
                isw)

        Unit
    }

    @Test
    fun `Views should be updated whenever a foreign model is updated`() = runBlocking {
        initRepos()
        createMikeAndTestCourse()

        val mike = userRepo.findById("mike")!!

        userRepo.update(mike.copy(name = "Mike2"))

        val isw = inscriptionStatsView.findOneBy(InscriptionStatsView::inscriptionId eq "insc_1")
        assertEquals(InscriptionStatsView("insc_1",
                "mike", "Mike2",
                "test_course", "Test course", 0),
                isw)
        Unit
    }
}
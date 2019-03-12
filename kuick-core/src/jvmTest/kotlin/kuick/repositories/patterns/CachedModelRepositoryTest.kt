package kuick.repositories.patterns

import kotlinx.coroutines.runBlocking
import kuick.repositories.ModelRepository
import kuick.repositories.eq
import kuick.repositories.memory.ModelRepositoryMemory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


class CachedModelRepositoryTest {


    class MemoryCache: Cache {

        private val map: MutableMap<String, Any> = mutableMapOf()

        override suspend fun <T : Any> get(key: String): T? {
            val cached = map.get(key)
            return cached?.let { it as T? }
        }

        override suspend fun <T : Any> put(key: String, cached: T) {
            map.put(key, cached)
        }

        override suspend fun remove(key: String) {
            map.remove(key)
        }

    }


    data class Question(val ebookId: String, val questionId: String, val question: String)

    @Test
    fun `caching items works`() = runBlocking {
        val cachedRepo = buildTestRepo()
        val repo = cachedRepo.repo

        // Actual repo is updated
        assertEquals(3, repo.findBy(Question::ebookId eq "e1").size)
        assertEquals(2, repo.findBy(Question::ebookId eq "e2").size)

        // Cached repo caches results
        assertEquals(3, cachedRepo.findBy(Question::ebookId eq "e1").size)
        assertEquals(2, cachedRepo.findBy(Question::ebookId eq "e2").size)
    }

    @Test
    fun `updating cached items works`() = runBlocking {
        val cachedRepo = buildTestRepo()
        val repo = cachedRepo.repo

        val e1Questions = cachedRepo.findBy(Question::ebookId eq "e1")
        val q11 = cachedRepo.findById("q11")!!
        cachedRepo.update(q11.copy(question = "Changed question 11"))
        val updatedE1Questions = cachedRepo.findBy(Question::ebookId eq "e1")
        assertNotEquals(e1Questions, updatedE1Questions)
    }

    @Test
    fun `cached subsets work`() = runBlocking {
        val cachedRepo = buildTestRepo()
        val repo = cachedRepo.repo

        // Cache elements...
        assertEquals(3, cachedRepo.findBy(Question::ebookId eq "e1").size)
        assertEquals(2, cachedRepo.findBy(Question::ebookId eq "e2").size)

        // Removing elements from actual repo doesn't affect the cached results
        repo.delete("q11")
        assertEquals(2, repo.findBy(Question::ebookId eq "e1").size)
        assertEquals(2, repo.findBy(Question::ebookId eq "e2").size)
        assertEquals(3, cachedRepo.findBy(Question::ebookId eq "e1").size)
        assertEquals(2, cachedRepo.findBy(Question::ebookId eq "e2").size)
    }


    private suspend fun buildTestRepo(): CachedModelRepository<String, Question> {
        val repo: ModelRepository<String, Question> = ModelRepositoryMemory(Question::class, Question::questionId)
        val cache = MemoryCache()
        val cachedRepo = CachedModelRepository(Question::class, Question::questionId, repo, cache, Question::ebookId)

        val q11 = cachedRepo.insert(Question("e1", "q11", "question 11"))
        val q12 = cachedRepo.insert(Question("e1", "q12", "question 12"))
        val q13 = cachedRepo.insert(Question("e1", "q13", "question 13"))

        val q21 = cachedRepo.insert(Question("e2", "q21", "question 21"))
        val q22 = cachedRepo.insert(Question("e2", "q22", "question 22"))

        return cachedRepo
    }
}
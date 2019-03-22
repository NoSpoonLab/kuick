package kuick.repositories.elasticsearch

import kotlinx.coroutines.runBlocking
import kuick.repositories.elasticsearch.orm.ElasticSearchFieldType.TEXT
import kuick.repositories.elasticsearch.orm.field
import kuick.repositories.eq
import kuick.repositories.like
import org.assertj.core.api.Assertions.assertThat
import org.jboss.arquillian.junit.*
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class ModelRepositoryElasticSearchTest : AbstractITTestWithElasticSearch() {
    data class TestModel(
        val id: String,
        val val1: String,
        val val2: Int,
        val val3: Boolean
    )

    private val modelRepositoryElasticSearch by lazy {
        ModelRepositoryElasticSearch(
                TestModel::class,
                TestModel::id,
                injector.getInstance(IndexClient::class.java),
                {
                    TestModel::val1 to field(TestModel::val1.name, TEXT)
                }
        )
    }

    @Test
    internal fun `should init properly`() = runBlocking {
        assertFalse(modelRepositoryElasticSearch.indexExists())
        modelRepositoryElasticSearch.init()
        assertTrue(modelRepositoryElasticSearch.indexExists())
    }

    @Test
    internal fun `should insert`() = runBlocking {
        modelRepositoryElasticSearch.init()
        assertEquals(0, modelRepositoryElasticSearch.getAll().size)

        val toInsert = TestModel("1", "test", 0, true)

        modelRepositoryElasticSearch.insert(toInsert)

        val result = modelRepositoryElasticSearch.getAll()
        assertEquals(1, result.size)
        assertEquals(toInsert, result[0])
    }


    @Test
    internal fun `should insertMany`() = runBlocking {
        modelRepositoryElasticSearch.init()
        assertEquals(0, modelRepositoryElasticSearch.getAll().size)

        val toInsert = setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", 2, true),
            TestModel("3", "test3", 3, true),
            TestModel("4", "test4", 4, true)
        )
        modelRepositoryElasticSearch.insertMany(toInsert)

        val result = modelRepositoryElasticSearch.getAll()
        assertThat(result)
            .containsAll(toInsert)
            .hasSameSizeAs(toInsert)
        Unit
    }

    @Test
    internal fun `should findById`() = runBlocking {
        modelRepositoryElasticSearch.init()
        assertEquals(0, modelRepositoryElasticSearch.getAll().size)

        val searchedModel = TestModel("1", "test1", 1, true)
        val toInsert = setOf(
            searchedModel,
            TestModel("2", "test2", 2, true),
            TestModel("3", "test3", 3, true),
            TestModel("4", "test4", 4, true)
        )
        modelRepositoryElasticSearch.insertMany(toInsert)

        val result = modelRepositoryElasticSearch.findById(searchedModel.id)
        assertThat(result).isEqualTo(searchedModel)
        Unit
    }

    @Test
    internal fun `should findOneBy`() = runBlocking {
        modelRepositoryElasticSearch.init()
        assertEquals(0, modelRepositoryElasticSearch.getAll().size)

        val searchedModel = TestModel("1", "test1", 1, true)
        val toInsert = setOf(
            searchedModel,
            TestModel("2", "test2", 2, true),
            TestModel("3", "test3", 3, true),
            TestModel("4", "test4", 4, true)
        )
        modelRepositoryElasticSearch.insertMany(toInsert)

        val result = modelRepositoryElasticSearch.findOneBy(TestModel::val2 eq searchedModel.val2)
        assertThat(result).isEqualTo(searchedModel)
        Unit
    }

    @Test
    internal fun `should findBy`() = runBlocking {
        modelRepositoryElasticSearch.init()
        assertEquals(0, modelRepositoryElasticSearch.getAll().size)

        val searchedModels = setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", 2, true)
        )
        val toInsert = searchedModels + setOf(
            TestModel("3", "test3", 3, false),
            TestModel("4", "test4", 4, false)
        )
        modelRepositoryElasticSearch.insertMany(toInsert)

        val result = modelRepositoryElasticSearch.findBy(TestModel::val3 eq true)
        assertThat(result)
            .containsAll(searchedModels)
            .hasSameSizeAs(searchedModels)
        Unit
    }

    @Test
    internal fun `should update`() = runBlocking {
        modelRepositoryElasticSearch.init()
        assertEquals(0, modelRepositoryElasticSearch.getAll().size)

        val toInsert = TestModel("1", "test", 0, true)

        modelRepositoryElasticSearch.insert(toInsert)

        val result1 = modelRepositoryElasticSearch.getAll()
        assertEquals(1, result1.size)
        assertEquals(toInsert, result1[0])

        val toUpdate = TestModel("1", "test2", 2, false)

        modelRepositoryElasticSearch.update(toUpdate)

        val result2 = modelRepositoryElasticSearch.getAll()
        assertEquals(1, result2.size)
        assertEquals(toUpdate, result2[0])

    }

    @Test
    internal fun `should updateMany`() = runBlocking {
        modelRepositoryElasticSearch.init()
        assertEquals(0, modelRepositoryElasticSearch.getAll().size)

        val toInsert = setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", 2, true),
            TestModel("3", "test3", 3, true),
            TestModel("4", "test4", 4, true)
        )
        modelRepositoryElasticSearch.insertMany(toInsert)

        val result1 = modelRepositoryElasticSearch.getAll()
        assertThat(result1)
            .containsAll(toInsert)
            .hasSameSizeAs(toInsert)

        val toUpdate = setOf(
            TestModel("1", "test1-1", 11, false),
            TestModel("2", "test2-1", 12, false),
            TestModel("3", "test3-1", 13, false),
            TestModel("4", "test4-1", 14, false)
        )
        modelRepositoryElasticSearch.updateMany(toUpdate)

        val result2 = modelRepositoryElasticSearch.getAll()
        assertThat(result2)
            .containsAll(toUpdate)
            .hasSameSizeAs(toUpdate)
        Unit
    }

    @Ignore
    @Test
    internal fun `should updateBy`() = runBlocking {
        modelRepositoryElasticSearch.init()
        assertEquals(0, modelRepositoryElasticSearch.getAll().size)

        val toInsert = setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", 2, true),
            TestModel("3", "test3", 3, false),
            TestModel("4", "test4", 4, false)
        )
        modelRepositoryElasticSearch.insertMany(toInsert)

        val result1 = modelRepositoryElasticSearch.getAll()
        assertThat(result1)
            .containsAll(toInsert)
            .hasSameSizeAs(toInsert)

        val toUpdate = TestModel("1", "test1", 1, true)

        modelRepositoryElasticSearch.updateBy(toUpdate, TestModel::val3 eq false)

        val afterUpdate = setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", 2, true),
            TestModel("1", "test1", 1, true),
            TestModel("1", "test1", 1, true)
        )

        val result2 = modelRepositoryElasticSearch.getAll()
        assertThat(result2)
            .containsAll(afterUpdate)
            .hasSameSizeAs(afterUpdate)
        Unit
    }

    @Test
    internal fun `should delete`() = runBlocking {
        modelRepositoryElasticSearch.init()
        assertEquals(0, modelRepositoryElasticSearch.getAll().size)

        val toInsert = TestModel("1", "test", 0, true)

        modelRepositoryElasticSearch.insert(toInsert)

        val result1 = modelRepositoryElasticSearch.getAll()
        assertEquals(1, result1.size)
        assertEquals(toInsert, result1[0])

        modelRepositoryElasticSearch.delete(toInsert.id)

        val result2 = modelRepositoryElasticSearch.getAll()
        assertEquals(0, result2.size)
    }

    @Test
    internal fun `should deleteBy`() = runBlocking {
        modelRepositoryElasticSearch.init()
        assertEquals(0, modelRepositoryElasticSearch.getAll().size)

        val toInsert = setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", 2, true),
            TestModel("3", "test3", 3, false),
            TestModel("4", "test4", 4, false)
        )
        modelRepositoryElasticSearch.insertMany(toInsert)

        val result1 = modelRepositoryElasticSearch.getAll()
        assertThat(result1)
            .containsAll(toInsert)
            .hasSameSizeAs(toInsert)

        modelRepositoryElasticSearch.deleteBy(TestModel::val3 eq false)

        val afterDelete = setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", 2, true)
        )

        val result2 = modelRepositoryElasticSearch.getAll()
        assertThat(result2)
            .containsAll(afterDelete)
            .hasSameSizeAs(afterDelete)
        Unit
    }

    @Test
    internal fun `should searchBy`() = runBlocking {
        modelRepositoryElasticSearch.init()
        assertEquals(0, modelRepositoryElasticSearch.getAll().size)

        val keyword = "keyword"
        val searchedModels = setOf(
            TestModel("1", "text containing $keyword", 1, true),
            TestModel("3", "text with $keyword also", 3, true)
        )
        val toInsert = searchedModels + setOf(
            TestModel("2", "some random text", 2, true),
            TestModel("4", "and another random text here", 4, true)
        )
        modelRepositoryElasticSearch.insertMany(toInsert)

        val result = modelRepositoryElasticSearch.searchBy(TestModel::val1 like keyword)
        assertThat(result.map { it.model })
            .containsAll(searchedModels)
            .hasSameSizeAs(searchedModels)
        Unit
    }

}
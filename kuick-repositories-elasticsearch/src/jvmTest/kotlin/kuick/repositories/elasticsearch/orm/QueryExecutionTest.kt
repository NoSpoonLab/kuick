package kuick.repositories.elasticsearch.orm

import kotlinx.coroutines.runBlocking
import kuick.repositories.*
import kuick.repositories.elasticsearch.AbstractITTestWithElasticSearch
import kuick.repositories.elasticsearch.IndexClient
import kuick.repositories.elasticsearch.ModelRepositoryElasticSearch
import kuick.repositories.elasticsearch.orm.ElasticSearchFieldType.TEXT
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class QueryExecutionTest : AbstractITTestWithElasticSearch() {

    data class TestModel(
        val id: String,
        val val1: String,
        val val2: Int?,
        val val3: Boolean
    )

    class TestModelIndexCreationEvent

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

    private fun testQuery(
        given: Collection<TestModel>,
        q: ModelQuery<TestModel>,
        expected: Collection<TestModel>
    ): Unit = runBlocking {
        modelRepositoryElasticSearch.init()
        modelRepositoryElasticSearch.insertMany(given)
        assertThat(modelRepositoryElasticSearch.findBy(q))
            .containsExactlyInAnyOrderElementsOf(expected)
        Unit
    }

    private fun testQuery(
        q: ModelQuery<TestModel>
    ): Unit = runBlocking {
        //        modelRepositoryElasticSearch.init()
        modelRepositoryElasticSearch.findBy(q)
        Unit
    }

    @Test
    internal fun `eq`() = testQuery(
        setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", 2, true),
            TestModel("3", "test3", 3, false),
            TestModel("4", "test4", 4, false)
        ),
        TestModel::val3 eq true,
        setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", 2, true)
        )
    )

    @Test(expected = NotImplementedError::class)
    internal fun `not`() = testQuery(
        not(TestModel::val3 eq true)
    )

    @Test
    internal fun `and`() = testQuery(
        setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", 2, true),
            TestModel("3", "test3", 1, false),
            TestModel("4", "test4", 2, false)
        ),
        (TestModel::val3 eq true) and (TestModel::val2 eq 2),
        setOf(
            TestModel("2", "test2", 2, true)
        )
    )

    @Test(expected = NotImplementedError::class)
    internal fun `or`() = testQuery(
        (TestModel::val3 eq true) or (TestModel::val2 eq 2)
    )

    @Test
    internal fun `is null`() = testQuery(
        setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", null, true),
            TestModel("3", "test3", null, false),
            TestModel("4", "test4", 4, false)
        ),
        TestModel::val2.isNull(),
        setOf(
            TestModel("2", "test2", null, true),
            TestModel("3", "test3", null, false)
        )
    )

    @Test
    internal fun `like`() {
        val keyword = "keyword"
        testQuery(
            setOf(
                TestModel("1", "this sentence contains $keyword", null, true),
                TestModel("2", "this is random sentence", 2, true),
                TestModel("3", "here is $keyword too", null, false),
                TestModel("4", "and here there is not", 2, false)
            ),
            TestModel::val1 like keyword,
            setOf(
                TestModel("1", "this sentence contains $keyword", null, true),
                TestModel("3", "here is $keyword too", null, false)
            )
        )
    }

    @Test
    internal fun `gt`() = testQuery(
        setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", 2, true),
            TestModel("3", "test3", 3, false),
            TestModel("4", "test4", 4, false)
        ),
        TestModel::val2 gt 2,
        setOf(
            TestModel("3", "test3", 3, false),
            TestModel("4", "test4", 4, false)
        )
    )

    @Test
    internal fun `gte`() = testQuery(
        setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", 2, true),
            TestModel("3", "test3", 3, false),
            TestModel("4", "test4", 4, false)
        ),
        TestModel::val2 gte 2,
        setOf(
            TestModel("2", "test2", 2, true),
            TestModel("3", "test3", 3, false),
            TestModel("4", "test4", 4, false)
        )
    )

    @Test
    internal fun `lt`() = testQuery(
        setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", 2, true),
            TestModel("3", "test3", 3, false),
            TestModel("4", "test4", 4, false)
        ),
        TestModel::val2 lt 2,
        setOf(
            TestModel("1", "test1", 1, true)
        )
    )

    @Test
    internal fun `lte`() = testQuery(
        setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", 2, true),
            TestModel("3", "test3", 3, false),
            TestModel("4", "test4", 4, false)
        ),
        TestModel::val2 lte 2,
        setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", 2, true)
        )
    )

    @Test
    internal fun `range using gt and lt`() = testQuery(
        setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", 2, true),
            TestModel("3", "test3", 3, false),
            TestModel("4", "test4", 4, false)
        ),
        (TestModel::val2 gte 2) and (TestModel::val2 lte 3),
        setOf(
            TestModel("2", "test2", 2, true),
            TestModel("3", "test3", 3, false)
        )
    )

    @Test
    internal fun `within`() = testQuery(
        setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", 2, true),
            TestModel("3", "test3", 3, false),
            TestModel("4", "test4", 4, false)
        ),
        TestModel::val2 within setOf(1, 2),
        setOf(
            TestModel("1", "test1", 1, true),
            TestModel("2", "test2", 2, true)
        )
    )
}



package kuick.repositories.elasticsearch.orm

import kuick.repositories.and
import kuick.repositories.elasticsearch.orm.ElasticSearchFieldType.TEXT
import kuick.repositories.eq
import kuick.repositories.like
import kuick.repositories.within
import org.elasticsearch.index.query.QueryBuilders.*
import org.junit.Test
import kotlin.test.assertEquals


class QueryGenerationTest {

    data class TestModel(val id: String, val val1: String, val val2: String)

    @Test
    fun `should transform eq query`() {
        val schema = ElasticSearchIndexSchema.fromClass(
            TestModel::class
        ) {
            TestModel::val1 to field("val1", TEXT)
        }
        val q = (TestModel::val1 eq "something1") and (TestModel::val2 eq "something2")

        val esQuery = q.toElasticSearch(schema)

        assertEquals(
            boolQuery().must(matchQuery("val1", "something1")).filter(termQuery("val2", "something2")),
            esQuery
        )
    }

    @Test
    fun `should transform like query`() {
        val schema = ElasticSearchIndexSchema.fromClass(
            TestModel::class
        ) {
            TestModel::val1 to field("val1", TEXT)
        }
        val q = (TestModel::val1 like "something1") and (TestModel::val2 like "something2")

        val esQuery = q.toElasticSearch(schema)

        assertEquals(
            boolQuery().must(matchPhrasePrefixQuery("val1", "something1")).filter(matchPhrasePrefixQuery("val2", "something2")),
            esQuery
        )
    }

    @Test
    fun `should transform within query`() {
        val schema = ElasticSearchIndexSchema.fromClass(TestModel::class)
        val q = TestModel::val1 within setOf("something1", "something2")

        val esQuery = q.toElasticSearch(schema)

        assertEquals(
            boolQuery().filter(termsQuery("val1", setOf("something1", "something2"))),
            esQuery
        )
    }

    @Test
    fun `should transform and query`() {
        val schema = ElasticSearchIndexSchema.fromClass(TestModel::class)
        val q = (TestModel::val1 eq "something1") and (TestModel::val2 eq "test")

        val esQuery = q.toElasticSearch(schema)

        assertEquals(
            boolQuery().filter(termQuery("val1", "something1")).filter(termQuery("val2", "test")),
            esQuery
        )
    }

}
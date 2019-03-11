package kuick.repositories.elasticsearch.orm

import kuick.repositories.elasticsearch.orm.ElasticSearchFieldType.KEYWORD
import kuick.repositories.elasticsearch.orm.ElasticSearchFieldType.TEXT
import org.junit.Test
import kotlin.test.assertEquals


class SchemaGenerationTest {

    data class TestModel(val id: String, val val1: String, val val2: Boolean)

    @Test
    fun `should generate schema`() {
        val schema = ElasticSearchIndexSchema.fromClass(TestModel::class)

        assertEquals(
            ElasticSearchIndexSchema<TestModel>().apply {
                TestModel::id to field("id", KEYWORD)
                TestModel::val1 to field("val1", KEYWORD)
                TestModel::val2 to field("val2", KEYWORD)
            }.getMapping(),
            schema.getMapping()
        )
    }

    @Test
    fun `should generate schema with custom fields`() {
        val schema = ElasticSearchIndexSchema.fromClass(
            TestModel::class
        ) {
            TestModel::val1 to field("val1", TEXT)
        }

        assertEquals(
            ElasticSearchIndexSchema<TestModel>().apply {
                TestModel::id to field("id", KEYWORD)
                TestModel::val1 to field("val1", TEXT)
                TestModel::val2 to field("val2", KEYWORD)
            }.getMapping(),
            schema.getMapping()
        )
    }
}
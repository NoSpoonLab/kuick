package kuick.repositories.elasticsearch.orm

import kuick.repositories.*
import kuick.repositories.elasticsearch.orm.ElasticSearchFieldType.KEYWORD
import kuick.repositories.elasticsearch.orm.ElasticSearchFieldType.TEXT
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders.*

fun <T : Any> ModelQuery<T>.toElasticSearch(
    schema: ElasticSearchIndexSchema<T>
): BoolQueryBuilder {

    fun ModelQuery<T>.process(previousQuery: BoolQueryBuilder): BoolQueryBuilder =
        previousQuery.let {
            when (this) {
                is FieldLike -> when (val type = schema.get(field)!!.type) {
                    TEXT -> it.must(matchPhrasePrefixQuery(field.name, value))
                    KEYWORD -> it.filter(matchPhrasePrefixQuery(field.name, value))
                    else -> throw NotImplementedError("Missing implementation of .toElasticSearch() for ${this} with type $type")
                }
                is FieldEqs<T, *> -> when (val type = schema.get(field)!!.type) {
                    TEXT -> it.must(matchQuery(field.name, value))
                    KEYWORD -> it.filter(termQuery(field.name, value))
                    else -> throw NotImplementedError("Missing implementation of .toElasticSearch() for ${this} with type $type")
                }
                is FieldGt<T, *> -> it.filter(rangeQuery(field.name).gt(value))
                is FieldGte<T, *> -> it.filter(rangeQuery(field.name).gte(value))
                is FieldLt<T, *> -> it.filter(rangeQuery(field.name).lt(value))
                is FieldLte<T, *> -> it.filter(rangeQuery(field.name).lte(value))
                is FieldWithin<T, *> -> it.filter(termsQuery(field.name, value))
                is FieldWithinComplex<T, *> -> it.filter(termsQuery(field.name, value))
                is FieldIsNull<T, *> -> it.mustNot(existsQuery(field.name))

                is FilterExpAnd<T> -> right.process(left.process(it))

                else -> throw NotImplementedError("Missing implementation of .toElasticSearch() for ${this}")
            }
        }

    return this.process(boolQuery())

}

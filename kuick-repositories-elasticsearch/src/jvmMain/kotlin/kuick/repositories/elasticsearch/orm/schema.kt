package kuick.repositories.elasticsearch.orm


import kuick.repositories.annotations.*
import kuick.repositories.elasticsearch.orm.ElasticSearchFieldType.KEYWORD
import kuick.utils.nonStaticFields
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

class ElasticSearchIndexSchema<T : Any?> {

    private val _map: MutableMap<KProperty1<T, *>, ElasticSearchFieldDefinition> = mutableMapOf()

    infix fun <R : Any?> KProperty1<T, R>.to(cd: ElasticSearchFieldDefinition) {
        _map.put(this, cd)
    }

    fun <R : Any?> get(field: KProperty1<T, R>): ElasticSearchFieldDefinition? = _map[field]

    fun getMapping(): Map<String, Any> =
        mapOf(
            "properties" to _map.map {
                it.value.name to mapOf(
                    "type" to it.value.type.name.toLowerCase()
                )
            }.toMap()
        )

    companion object {
        fun <T> empty() = ElasticSearchIndexSchema<T>()

        fun <T : Any> fromClass(
            modelClass: KClass<T>,
            customMappings: ElasticSearchIndexSchema<T>.() -> Unit = {}
        ): ElasticSearchIndexSchema<T> =
            ElasticSearchIndexSchema<T>().apply {
                customMappings(this)
                modelClass.java.nonStaticFields()
                    .map { field ->
                        modelClass.declaredMemberProperties.firstOrNull { it.name == field.name }
                            ?: throw IllegalStateException("Property not found for field: ${field.name}")
                    }.filter {
                        this.get(it) == null
                    }.forEach { prop ->
                        val maxLength = prop.findAnnotation<MaxLength>()?.maxLength
                        val nullableProp = prop.returnType.isMarkedNullable
                        val returnType = prop.returnType.classifier!!.starProjectedType
                        val columnName = prop.name
                        val columnType = KEYWORD
                        //println("Registering field ${prop} with return type: ${prop.returnType}")
                        with(this) {
                            prop to field(columnName, columnType)
                        }
                    }
            }
    }
}

enum class ElasticSearchFieldType {
    KEYWORD, TEXT, DATE, LONG, DOUBLE, BOOLEAN, IP,
    OBJECT, NESTED,
    GEO_POINT, GEO_SHAPE, COMPLETION
}

data class ElasticSearchFieldDefinition(
    val name: String,
    val type: ElasticSearchFieldType
)

fun <T> ElasticSearchIndexSchema<T>.field(name: String, type: ElasticSearchFieldType): ElasticSearchFieldDefinition {
    return ElasticSearchFieldDefinition(name, type)
}

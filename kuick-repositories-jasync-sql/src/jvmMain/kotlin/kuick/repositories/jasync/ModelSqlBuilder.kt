package kuick.repositories.jasync

import kuick.models.Id
import kuick.repositories.*
import kuick.utils.nonStaticFields
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.*

class ModelSqlBuilder<T: Any>(val kClass: KClass<T>, val tableName: String) {

    data class PreparedSql(val sql: String, val values: List<Any?>)

    private val modelFields: List<Field> = kClass.java.nonStaticFields()

    private val modelProperties = modelFields
        .map { field -> kClass.memberProperties.first { it.name == field.name } }

    private fun String.toSnakeCase(): String = flatMap {
        if (it.isUpperCase()) listOf('_', it.toLowerCase()) else listOf(it)
    }.joinToString("")


    val modelColumns = modelFields.map { it.name.toSnakeCase() }

    protected val selectColumns = modelColumns.joinToString(", ")

    protected val insertColumns = selectColumns
    protected val insertValueSlots = modelColumns.map { "?" }.joinToString(", ")

    protected val updateColumns = modelColumns.map { "$it = ?" }.joinToString(", ")

    protected val selectBase = "SELECT $selectColumns FROM $tableName"

    fun selectSql(q: ModelQuery<T>) = "$selectBase WHERE ${toSql(q, this::toSqlValue)}"

    fun selectPreparedSql(q: ModelQuery<T>): PreparedSql {
        val base = PreparedSql("$selectBase WHERE ${toSql(q, this::toSlotValue)}", queryValues(q))
        val extraSql = mutableListOf<String>()

        q.tryGetAttributed()?.let { q ->
            if (q.orderBy != null) extraSql.add("ORDER BY ${q.orderBy!!.list.map { "${it.prop.name.toSnakeCase()} ${if (it.ascending) "ASC" else "DESC"}" }.joinToString(", ")}")
            if (q.skip > 0) extraSql.add("SKIP ${q.skip}")
            if (q.limit != null) extraSql.add("LIMIT ${q.limit}")
        }

        return base.copy("${base.sql} ${extraSql.joinToString(" ")}".trim(), base.values)
    }

    val insertSql = "INSERT INTO $tableName ($insertColumns) VALUES ($insertValueSlots)"
    fun insertPreparedSql(t: T): PreparedSql =
        PreparedSql(insertSql, valuesOf(t))

    fun insertManySql(ts: Collection<T>): String =
        "INSERT INTO $tableName ($insertColumns) VALUES ${ts.map { "(${valuesOf(it).map { toSqlValue(it) }.joinToString(", ")})" }.joinToString(", ")}"

    fun updateSql(q: ModelQuery<T>) = "UPDATE $tableName SET $updateColumns WHERE ${toSql(q, this::toSqlValue)}"

    fun updatePreparedSql(t: T, q: ModelQuery<T>): PreparedSql =
        PreparedSql("UPDATE $tableName SET $updateColumns WHERE ${toSql(q, this::toSlotValue)}", valuesOf(t) + queryValues(q))

    fun deleteSql(q: ModelQuery<T>) = "DELETE FROM $tableName WHERE ${toSql(q, this::toSqlValue)}"

    fun deletePreparedSql(q: ModelQuery<T>): PreparedSql =
        PreparedSql("DELETE FROM $tableName WHERE ${toSql(q, this::toSlotValue)}", queryValues(q))



    fun toSql(q: ModelQuery<T>, toSqlValue: (Any?) -> String = this::toSqlValue): String = when (q) {
        is FieldIsNull<T, *> -> "${q.field.name.toSnakeCase()} IS NULL"
        is FieldWithin<T, *> -> "${q.field.name.toSnakeCase()} in (${(q.value ?: emptySet()).map { toSqlValue(it) }.joinToString(", ")})"
        is FieldWithinComplex<T, *> -> "${q.field.name.toSnakeCase()} in (${(q.value ?: emptySet()).map { toSqlValue(it) }.joinToString(", ")})"
        is FilterExpUnopLogic<T> -> "${q.op}(${toSql(q.exp, toSqlValue)})"

        is SimpleFieldBinop<T, *> -> "${q.field.name.toSnakeCase()} ${q.op} ${toSqlValue(q.value)}"
        is FilterExpAnd<T> -> "(${toSql(q.left, toSqlValue)}) ${q.op} (${toSql(q.right, toSqlValue)})"
        is FilterExpOr<T> -> "(${toSql(q.left, toSqlValue)}) ${q.op} (${toSql(q.right, toSqlValue)})"

        is DecoratedModelQuery<T> -> toSql(q.base, toSqlValue) // Ignore
        else -> throw NotImplementedError("Missing implementation of .toSql() for ${q}")
    }

    fun toSlotValue(value: Any?): String = "?"

    fun toSqlValue(value: Any?): String = when (value) {
        null -> "NULL"
        is Boolean, is Int, is Long, is Float, is Double -> value.toString()
        is Id -> "'${value.id}'"
        else -> "'${value.toString().replace("'", "''")}'" // Escape single quotes
    }

    fun toDbValue(value: Any?): Any? = when {
        value is Id -> value.id
        else -> value
    }


    // TODO Permitir un "mapper" en el constructor para convertir propiedades
    fun valuesOf(t: T): List<Any?> = modelProperties.map { prop -> toDbValue(prop.get(t)) }

    /**
     * Builds a model from a list of values
     */
    fun modelFromValues(fieldValues: List<Any?>): T  {
        val constructor = kClass.constructors.first()
        try {
            val values = constructor.parameters.mapIndexed { index, prop ->
                val dbValue = fieldValues[index]
                when {
                    prop.type.clazz.isSubclassOf(Id::class) -> prop.type.clazz.primaryConstructor?.call(dbValue) as? Id?
                    else -> dbValue
                }
            }
            return constructor.call(*values.toTypedArray())
        } catch (t: Throwable) {
            System.err.println("MAPPING ERROR ------------")
            System.err.println("Constructor: ${constructor}")
            System.err.println("SQL results: ${fieldValues}")
            System.err.println("\n")
            throw t
        }
    }

    val KType.clazz get() = classifier as KClass<*>



    fun queryValues(q: ModelQuery<T>): List<Any?> = when (q) {
        is FieldIsNull<T, *> -> emptyList()
        is FieldWithin<T, *> -> q.value?.toList() ?: emptyList()
        is FieldWithinComplex<T, *> -> q.value?.map { toDbValue(it) } ?: emptyList()
        is FilterExpUnopLogic<T> -> queryValues(q.exp)

        is SimpleFieldBinop<T, *> -> listOf(toDbValue(q.value))
        is FilterExpAnd<T> -> queryValues(q.left) + queryValues(q.right)
        is FilterExpOr<T> -> queryValues(q.left) + queryValues(q.right)

        is DecoratedModelQuery<T> -> queryValues(q.base) // Ignore
        else -> throw NotImplementedError("Missing implementation of .toSql() for ${q}")
    }
}

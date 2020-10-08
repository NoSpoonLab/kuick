package kuick.repositories.jasync

import kuick.models.Id
import kuick.repositories.AttributedModelQuery
import kuick.repositories.DecoratedModelQuery
import kuick.repositories.FieldWithin
import kuick.repositories.FilterExpAnd
import kuick.repositories.FilterExpOr
import kuick.repositories.FilterExpUnopLogic
import kuick.repositories.ModelQuery
import kuick.repositories.SimpleFieldBinop
import kuick.repositories.tryGetAttributed
import kuick.utils.nonStaticFields
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

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
            if (q.skip > 0) extraSql.add("SKIP ${q.skip}")
            if (q.limit != null) extraSql.add("LIMIT ${q.limit}")
            if (q.orderBy != null) extraSql.add("ORDER BY ${q.orderBy!!.list.map { "${it.prop.name.toSnakeCase()} ${if (it.ascending) "ASC" else "DESC"}" }.joinToString(", ")}")
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
        is FieldWithin<T, *> -> "${q.field.name.toSnakeCase()} in (${(q.value ?: emptySet()).map { toSqlValue(it) }.joinToString(", ")})"
        is FilterExpUnopLogic<T> -> "${q.op}(${toSql(q.exp, toSqlValue)})"

        is SimpleFieldBinop<T, *> -> "${q.field.name.toSnakeCase()} ${q.op} ${toSqlValue(q.value)}"
        is FilterExpAnd<T> -> "(${toSql(q.left, toSqlValue)}) ${q.op} (${toSql(q.right, toSqlValue)})"
        is FilterExpOr<T> -> "(${toSql(q.left, toSqlValue)}) ${q.op} (${toSql(q.right, toSqlValue)})"

        is DecoratedModelQuery<T> -> toSql(q.base, toSqlValue) // Ignore
        else -> throw NotImplementedError("Missing implementation of .toSql() for ${this}")
    }

    fun toSlotValue(value: Any?): String = "?"

    fun toSqlValue(value: Any?): String = when (value) {
        null -> "NULL"
        is Boolean, is Int, is Long, is Float, is Double -> value.toString()
        is Id -> "'${value.id}'"
        else -> "'${value.toString().replace("'", "''")}'" // Escape single quotes
    }


    // TODO Permitir un "mapper" en el constructor para convertir propiedades
    fun valuesOf(t: T): List<Any?> = modelProperties.map { prop -> prop.get(t) }

    /**
     * Builds a model from a list of values
     */
    fun modelFromValues(fieldValues: List<Any?>): T  {
        try {
            return kClass.constructors.first().call(*fieldValues.toTypedArray())
        } catch (t: Throwable) {
            System.err.println("MAPPING ERROR ------------")
            System.err.println("Constructor: ${kClass.constructors.first()}")
            System.err.println("SQL results: ${fieldValues}")
            System.err.println("\n")
            throw t
        }
    }



    fun queryValues(q: ModelQuery<T>): List<Any?> = when (q) {
        is FilterExpUnopLogic<T> -> queryValues(q.exp)

        is SimpleFieldBinop<T, *> -> listOf(q.value)
        is FilterExpAnd<T> -> queryValues(q.left) + queryValues(q.right)
        is FilterExpOr<T> -> queryValues(q.left) + queryValues(q.right)

        is DecoratedModelQuery<T> -> queryValues(q.base) // Ignore
        else -> throw NotImplementedError("Missing implementation of .toSql() for ${this}")
    }
}

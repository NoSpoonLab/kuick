package kuick.orm

import kuick.json.*
import kuick.models.*
import kuick.repositories.annotations.*
import kuick.utils.fastForEach
import java.sql.*
import java.text.*
import java.time.*
import java.util.Date
import kotlin.collections.LinkedHashMap
import kotlin.reflect.*
import kotlin.reflect.full.*

sealed class ColumnType {
    object INT : ColumnType()
    object TIMESTAMP : ColumnType()
    data class VARCHAR(val length: Int? = null) : ColumnType()
}

val KType.clazz get() = classifier as KClass<*>

class ColumnDefinition<T : Any>(val table: TableDefinition<T>, val prop: KProperty1<T, *>) {
    val serialization get() = table.serialization
    val name = prop.findAnnotation<DbName>()?.name ?: prop.name
    val unique = prop.findAnnotation<Unique>() != null
    val withIndex = prop.findAnnotation<Index>() != null
    val maxLength = prop.findAnnotation<MaxLength>()?.maxLength
    val type = prop.returnType
    val clazz = type.clazz
    val nullable = type.isMarkedNullable
    val strategy by lazy { serialization.resolve(this) ?: error("Can't find serialization strategy for $this") }
    val columnType by lazy { strategy.columnType(this) }
}

inline fun <reified T : Any> TableDefinition(serialization: TableSerializationStrategy = defaultTableSerializationStrategy) = TableDefinition(T::class, serialization)

class TableDefinitions(val serialization: TableSerializationStrategy = defaultTableSerializationStrategy) {
    private val definitions = LinkedHashMap<KClass<*>, TableDefinition<*>>()

    fun <T : Any> get(clazz: KClass<T>): TableDefinition<T> =
            definitions.getOrPut(clazz) { TableDefinition(clazz, serialization) } as TableDefinition<T>

    inline fun <reified T : Any> get(): TableDefinition<T> = get(T::class)
}

class TableDefinition<T : Any>(val clazz: KClass<T>, val serialization: TableSerializationStrategy = defaultTableSerializationStrategy) {
    val name = clazz.findAnnotation<DbName>()?.name ?: clazz.simpleName
    ?: error("Can't determine table name for $clazz")

    val columns = clazz.memberProperties.filter { it.visibility == KVisibility.PUBLIC }.map { ColumnDefinition(this, it) }
    val columnsByName = columns.associateBy { it.name }
    val columnsByProp = columns.associateBy { it.prop }

    operator fun get(prop: KProperty1<*, *>): ColumnDefinition<T> = columnsByProp[prop as KProperty1<T, *>] ?: error("Can't find $prop in $this")

    fun untype(instance: T): Map<String, Any?> {
        val out = LinkedHashMap<String, Any?>()
        for (prop in columns) {
            out[prop.name] = prop.strategy.serialize(prop, prop.prop.get(instance))
        }
        return out
    }

    fun type(map: Map<String, Any?>): T {
        val constructor = clazz.primaryConstructor!!
        val params = constructor.parameters.map {
            val columnName = it.name
            val column = columnsByName[columnName] ?: error("Can't find column ${it.name}")
            column.strategy.deserialize(column, map[column.name])
        }
        return constructor.call(*params.toTypedArray())
    }
}

interface TableSerializationStrategy {
    fun resolve(column: ColumnDefinition<*>): TableSerializationStrategy? = this
    fun columnType(column: ColumnDefinition<*>): ColumnType? {
        val resolved = resolve(column)
        return if (resolved != null && resolved != this) resolved.columnType(column) else ColumnType.VARCHAR()
    }

    fun serialize(column: ColumnDefinition<*>, value: Any?): Any? {
        val resolved = resolve(column)
        return if (resolved != null && resolved != this) resolved.serialize(column, value) else value
    }

    fun deserialize(column: ColumnDefinition<*>, value: Any?): Any? {
        val resolved = resolve(column)
        return if (resolved != null && resolved != this) resolved.deserialize(column, value) else value
    }
}

open class TypedTableSerializationStrategy(val clazz: KClass<*>) : TableSerializationStrategy {
    override fun resolve(column: ColumnDefinition<*>): TableSerializationStrategy? = if (column.clazz == clazz) this else null
}

class TypedTableSerializationStrategies(val types: Map<KClass<*>, TableSerializationStrategy>) : TableSerializationStrategy {
    constructor(types: List<TypedTableSerializationStrategy>) : this(types.map { it.clazz to it }.toMap())
    constructor(vararg types: TypedTableSerializationStrategy) : this(types.map { it.clazz to it }.toMap())

    override fun resolve(column: ColumnDefinition<*>): TableSerializationStrategy? = types[column.clazz]
}

class TableSerializationStrategies(val strategies: List<TableSerializationStrategy>) : TableSerializationStrategy {
    constructor(vararg strategies: TableSerializationStrategy) : this(strategies.toList())

    override fun resolve(column: ColumnDefinition<*>): TableSerializationStrategy? {
        strategies.fastForEach {
            val resolved = it.resolve(column)
            if (resolved != null) return resolved
        }
        return null
    }
}

object IntSerializationStrategy : TypedTableSerializationStrategy(Int::class) {
    override fun columnType(column: ColumnDefinition<*>): ColumnType? = ColumnType.INT
}

object StringSerializationStrategy : TypedTableSerializationStrategy(String::class) {
    override fun columnType(column: ColumnDefinition<*>): ColumnType? = ColumnType.VARCHAR(column.maxLength)
}

object LongDateSerializationStrategy : TypedTableSerializationStrategy(Date::class) {
    override fun columnType(column: ColumnDefinition<*>): ColumnType? = ColumnType.INT
    override fun serialize(column: ColumnDefinition<*>, value: Any?): Any? = (value as Date).time
    override fun deserialize(column: ColumnDefinition<*>, value: Any?): Any? = Date((value as Number).toLong())
}

object DateSerializationStrategy : TypedTableSerializationStrategy(Date::class) {
    //yyyy-MM-dd hh:mm:ss[.nnnnnnnnn]
    val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.000000SSS")
    override fun columnType(column: ColumnDefinition<*>): ColumnType? = ColumnType.TIMESTAMP
    override fun serialize(column: ColumnDefinition<*>, value: Any?): Any? {
        //return DATE_FORMAT.format(value as Date)
        return Timestamp.from((value as Date).toInstant())
    }
    override fun deserialize(column: ColumnDefinition<*>, value: Any?): Any? {
        if (value is Timestamp) return Date.from(value.toLocalDateTime().toInstant(ZoneOffset.UTC))
        return DATE_FORMAT.parse(value.toString())
    }
}

object IdSerializationStrategy : TableSerializationStrategy {
    override fun resolve(column: ColumnDefinition<*>): TableSerializationStrategy? = if (column.clazz.isSubclassOf(Id::class)) this else null
    override fun columnType(column: ColumnDefinition<*>): ColumnType? = ColumnType.VARCHAR(column.maxLength)
    override fun serialize(column: ColumnDefinition<*>, value: Any?): Any? = (value as Id).id
    override fun deserialize(column: ColumnDefinition<*>, value: Any?): Any? = column.clazz.primaryConstructor!!.call((value as String))
}

object JsonSerializationStrategy : TableSerializationStrategy {
    override fun resolve(column: ColumnDefinition<*>): TableSerializationStrategy? = this
    override fun columnType(column: ColumnDefinition<*>): ColumnType? = ColumnType.VARCHAR(column.maxLength)

    override fun serialize(column: ColumnDefinition<*>, value: Any?): Any? {
        val result = value?.let { Json.toJson(value) }
        return result
    }

    override fun deserialize(column: ColumnDefinition<*>, value: Any?): Any? = Json.fromJson(value as String, column.type)
}

private fun constructStandardTableSerializationStrategy(date: TypedTableSerializationStrategy): TableSerializationStrategy {
    return TableSerializationStrategies(
            TypedTableSerializationStrategies(
                    IntSerializationStrategy,
                    StringSerializationStrategy,
                    date
            ),
            IdSerializationStrategy,
            JsonSerializationStrategy
    )
}

val oldTableSerializationStrategy: TableSerializationStrategy = constructStandardTableSerializationStrategy(LongDateSerializationStrategy)
val defaultTableSerializationStrategy: TableSerializationStrategy = constructStandardTableSerializationStrategy(DateSerializationStrategy)
//val defaultTableSerializationStrategy: TableSerializationStrategy = constructStandardTableSerializationStrategy(LongDateSerializationStrategy)
//val dateTableSerializationStrategy = constructStandardTableSerializationStrategy(DateSerializationStrategy)

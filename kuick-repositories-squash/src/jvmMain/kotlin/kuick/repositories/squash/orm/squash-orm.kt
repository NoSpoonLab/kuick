package kuick.repositories.squash.orm

import kuick.db.DomainTransaction
import kuick.json.Json
import kuick.models.Email
import kuick.models.Id
import kuick.models.KLocalDate
import kuick.utils.nonStaticFields
import org.jetbrains.squash.connection.*
import org.jetbrains.squash.definition.ColumnDefinition
import org.jetbrains.squash.definition.Table
import org.jetbrains.squash.definition.TableDefinition
import org.jetbrains.squash.expressions.*
import org.jetbrains.squash.query.from
import org.jetbrains.squash.query.orderBy
import org.jetbrains.squash.query.where
import org.jetbrains.squash.results.*
import org.jetbrains.squash.statements.*
import java.io.*
import java.lang.reflect.Field
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.atomic.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import java.time.ZoneOffset


const val LOCAL_DATE_LEN = 16
const val LOCAL_DATE_TIME_LEN = 30
const val ID_LEN = 36
const val SHORT_TEXT_LEN = 512
const val LONG_TEXT_LEN = 4000
const val VERY_LONG_TEXT_LEN = 25000



typealias LazyDomainTransactionSquash = DomainTransactionSquash

class DomainTransactionSquash(val db: DatabaseConnection): DomainTransaction, Closeable {
    private var _tr = AtomicReference<Transaction?>(null)
    val tr: Transaction get() {
        if (_tr.get() == null) {
            val tr = db.createTransaction()
            _tr.set(tr)
        }
        return _tr.get()!!
    }

    override fun close() {
        _tr.get()?.close()
    }
}

infix fun <T:Any> ResultRow.toDAO(ormDef: ORMTableDefinition<T>): T {
    val fields = ormDef.clazz.toDAOFields()
    val fieldValues = fields.map { f ->
        try {
            val property = ormDef.clazz.memberProperties.first { it.name == f.name }
            val columnDef = ormDef.get(property)
            readColumnValue(ormDef.clazz, f, columnDef.name.id, ormDef.compoundName.id)
        } catch (t: Throwable) {
            throw IllegalStateException("Had a problem reading field ${f}", t)
        }
    }
    return ormDef.clazz.constructors.first().call(*fieldValues.toTypedArray())
}

private fun <T:Any> KClass<T>.toDAOFields() = java.nonStaticFields()

private inline fun <reified T> ResultRow.columnValue(columnName: String, tableName: String?  = null): T? =
        columnValue(T::class, columnName, tableName) as? T?

private inline fun <T> ignoreErrors(callback: () -> T): T? = runCatching { callback() }.getOrNull()

private fun <T:Any> ResultRow.readColumnValue(clazz: KClass<T>, field: Field, columnName: String, tableName: String): Any? = when (val type = field.type.kotlin) {
    String::class, Boolean::class, Int::class, Long::class, Float::class -> columnValue(type, columnName, tableName)
    Double::class, BigDecimal::class -> columnValue<BigDecimal>(columnName, tableName)?.toDouble()
    Email::class -> columnValue<String>(columnName, tableName)?.let { Email(it) }
    Date::class -> columnValue<LocalDateTime>(columnName, tableName)?.let {
        val zoneOffset = ZoneOffset.UTC.normalized().rules.getOffset(it)
        Date.from(it.toInstant(zoneOffset))
        }

    LocalDateTime::class -> columnValue<String>(columnName, tableName)?.let { LocalDateTime.parse(it, DATE_TIME_FORMAT) }
    LocalDate::class -> columnValue<String>(columnName, tableName)?.takeIf { it != "0000-00-00" }?.let { dateAsStr ->
        ignoreErrors { LocalDate.parse(dateAsStr, DATE_FORMAT) }
            ?: ignoreErrors { LocalDate.parse(Json.fromJson<KLocalDate>(dateAsStr).toString(), DATE_FORMAT) }
            ?: error("Unknown date format [$dateAsStr]")
    }
    else -> when {
        type.isSubclassOf(Id::class) -> columnValue<String>(columnName, tableName)?.let { type.primaryConstructor?.call(it) }
        else -> columnValue<String>(columnName, tableName)?.let { field.apply { isAccessible = true }.get(Json.fromJson("{\"${field.name}\":$it}", clazz)) }
    }
}


infix fun <D:Any, T : Table> InsertValuesStatement<T, Unit>.from(data: D) {
    val clazz = data::class.java
    clazz.nonStaticFields().withIndex().forEach { (i, f) ->
        f.isAccessible = true
        val value = f.get(data)
        val decodedValue = decodeValue(value)
        if (i >= table.compoundColumns.size) throw NotImplementedError("Missing SQUASH column definition in ${table.javaClass.simpleName} for field ${f}")
        val column = table.compoundColumns[i]
        set(column, decodedValue)
    }
}
infix fun <D:Any, T : Table> UpdateQueryStatement<T>.from(data: D) {
    val clazz = data::class.java
    clazz.nonStaticFields().withIndex().forEach { (i, f) ->
        f.isAccessible = true
        val value = f.get(data)
        val decodedValue = decodeValue(value)
        val column = table.compoundColumns[i]
        set(column, decodedValue)
    }
}

private val DATE_FORMAT = DateTimeFormatter.ISO_DATE
private val DATE_TIME_FORMAT = DateTimeFormatter.ISO_DATE_TIME

private fun decodeValue(value: Any?) = when (value) {
    null -> null
    is Id -> value.id
    is Email -> value.email
    is String, is Int, is Long, is Double -> value
    is Date ->  LocalDateTime.ofInstant(value.toInstant(),ZoneOffset.UTC.normalized())
    is LocalDate -> DATE_FORMAT.format(value)
    is LocalDateTime -> DATE_TIME_FORMAT.format(value)
    value::class.java == Int::class.java -> value
    is Boolean -> value.toString().toBoolean()
    else -> Json.toJson(value)
}

open class ORMTableDefinition<T : Any> (val clazz: KClass<T>, val tableName: String = clazz.simpleName!!): TableDefinition(tableName) {
    private val _map: MutableMap<KProperty1<T, *>, ColumnDefinition<*>> = mutableMapOf()

    infix fun <R: Any?> KProperty1<T, R>.to(cd: ColumnDefinition<R>) {
        _map.put(this, cd)
    }

    operator fun <R: Any?> get(p: KProperty1<T, R>): ColumnDefinition<R> {
        if (!_map.containsKey(p)) {
            throw Exception("Table[${clazz.simpleName}] has no mapping for ${p}")
        }
        return _map.get(p) as ColumnDefinition<R>
    }

    fun printMappings() {
        println("-------- [${clazz.simpleName}] Mappings -------------")
        println("Available mappings:")
        _map.forEach { t, u -> println("  - $t") }
        println("--------/[${clazz.simpleName}] Mappings -------------")
    }


    // INSERT ----------------------------

    fun insertAll(tr: DomainTransaction, items: Collection<T>) = items.forEach { item -> insert(tr, item) }
    fun insert(tr: DomainTransaction, item: T): T = insert(tr.squashTr(), item)

    private fun insert(tr: Transaction, item: T): T {
        insertInto(this).values { it.from(item) }.monitorAndExecuteOn(tr)
        return item
    }


    // SELECT ----------------------------

    fun selectAll(tr: DomainTransaction): List<T> = select(tr.squashTr())

    fun select(tr: DomainTransaction, predicate: () -> Expression<Boolean>): List<T> = select(tr.squashTr(), predicate)

    fun selectOrdered(tr: DomainTransaction, predicate: () -> Expression<Boolean>, ascending: Boolean, orderBy: () -> Expression<*>): List<T> = selectOrdered(tr.squashTr(), predicate, ascending, orderBy)

    fun selectOne(tr: DomainTransaction, predicate: () -> Expression<Boolean>): T? = selectOne(tr.squashTr(), predicate)

    private fun selectOne(tr: Transaction, predicate: () -> Expression<Boolean>): T? {
        val results = select(tr, predicate)
        return when {
            results.isEmpty() -> null
            results.size == 1 -> results.first()
            else -> {
                println("WARNING: Returning 1st of several results on a selectOne query: $predicate")
                results.first()
            }
        }
    }

    private fun select(tr: Transaction, predicate: (() -> Expression<Boolean>)? = null): List<T> =
            from(this).let {
                if(predicate != null)
                    it.where(predicate)
                else
                    it
            }.monitorAndExecuteOn(tr)

    private fun selectOrdered(tr: Transaction, predicate: () -> Expression<Boolean>,
                              ascending: Boolean, orderBy: () -> Expression<*>): List<T> =
        from(this).where(predicate).orderBy(ascending, orderBy).monitorAndExecuteOn(tr)


    // UPDATE ----------------------------

    fun update(tr: DomainTransaction, updated: T, where: () -> Expression<Boolean>): T =
            update(tr.squashTr(), updated, where)

    fun updateOrCreate(tr: DomainTransaction, where: () -> Expression<Boolean>, updateOrCreateFn: (T?) -> T): T =
            updateOrCreate(tr.squashTr(), where, updateOrCreateFn)

    private fun updateOrCreate(tr: Transaction, where: () -> Expression<Boolean>, updateOrCreateFn: (T?) -> T): T {
        val element = selectOne(tr, where)
        val finalElement = updateOrCreateFn(element)
        when (element) {
            null -> insert(tr, finalElement)
            else -> update(tr, finalElement, where)
        }
        return finalElement
    }

    private fun update(tr: Transaction, updated: T, where: () -> Expression<Boolean>): T {
        update(this).set { it from updated }.where(where).monitorAndExecuteOn(tr)
        return updated
    }


    // DELETE ----------------------------

    fun delete(tr: DomainTransaction, where: () -> Expression<Boolean>) = delete(tr.squashTr(), where)

    fun deleteAll(tr: DomainTransaction) = deleteFrom(this).monitorAndExecuteOn(tr.squashTr())

    private fun delete(tr: Transaction, where: () -> Expression<Boolean>) =
        deleteFrom(this).where(where).monitorAndExecuteOn(tr)



    // MONITOR AND EXECUTE! ----------------------------
    // Removed monitoring for now because it was a direct access to a static on another module.

    private val table = this

    private fun QueryStatement.monitorAndExecuteOn(tr: Transaction) =  executeOn(tr).map { it toDAO table }.toList()

    private fun <R:Any> Statement<R>.monitorAndExecuteOn(tr: Transaction) =   executeOn(tr)

}

fun DomainTransaction.squashTr() = (this as LazyDomainTransactionSquash).tr


infix fun <V: Any> ColumnDefinition<V>.eqEnum(literal: V?): Expression<Boolean> {

    return when {
        literal == null -> this.eq<V?>(null)
        literal is ColumnDefinition<*> -> this.eq(literal)
        else -> this.eq<V>(Json.toJson (literal) as V)
    }
}

infix fun <V : Any> ColumnDefinition<V>.withinEnum(literals: Collection<V>?): Expression<Boolean> {

    return when {
        literals == null -> literal(false)
        literals is ColumnDefinition<*> -> this.within(literals)
        else -> this.within<V>( literals.map { Json.toJson (it) as V })
    }
}

infix fun <V> ColumnDefinition<V>.eqId(literal: V?): Expression<Boolean> = when {
    literal == null -> this.eq<V?>(null)
    literal is ColumnDefinition<*> -> this.eq(literal)
    else -> this.eq<V>((literal as Id).id as V)
}

infix fun <V> ColumnDefinition<V>.eqLocalDate(literal: V?): Expression<Boolean> = when (literal) {
    null -> this.eq<V?>(null)
    else -> this.eq<V>((literal as LocalDate).toString() as V)
}

infix fun <V> ColumnDefinition<V>.gteqLocalDateTime(literal: V?): Expression<Boolean> = when (literal) {
    null -> this.gteq<V?>(null)
    else -> this.gteq<V>((literal as LocalDateTime).toString() as V)
}

infix fun <V> ColumnDefinition<V>.ltLocalDateTime(literal: V?): Expression<Boolean> = when (literal) {
    null -> this.lt<V?>(null)
    else -> this.lt<V>((literal as LocalDateTime).toString() as V)
}

package kuick.repositories.squash.orm

import kuick.core.*
import kuick.db.DomainTransaction
import kuick.json.Json
import kuick.models.Id
import kuick.repositories.*
import kuick.repositories.squash.*
import kuick.utils.nonStaticFields
import org.jetbrains.squash.connection.*
import org.jetbrains.squash.definition.ColumnDefinition
import org.jetbrains.squash.definition.Table
import org.jetbrains.squash.definition.TableDefinition
import org.jetbrains.squash.expressions.*
import org.jetbrains.squash.query.*
import org.jetbrains.squash.results.*
import org.jetbrains.squash.statements.*
import java.io.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*


const val LOCAL_DATE_LEN = 16
const val LOCAL_DATE_TIME_LEN = 30
const val ID_LEN = 36
const val SHORT_TEXT_LEN = 512
const val LONG_TEXT_LEN = 4000
const val VERY_LONG_TEXT_LEN = 25000

val DATE_FORMAT = DateTimeFormatter.ISO_DATE
val DATE_TIME_FORMAT = DateTimeFormatter.ISO_DATE_TIME

typealias LazyDomainTransactionSquash = DomainTransactionSquash

@KuickInternalWarning
class DomainTransactionSquash(val db: DatabaseConnection): DomainTransaction, Closeable {
    private var closed = false
    private var _tr = AtomicReference<Transaction?>(null)
    val tr: Transaction get() {
        if (closed) error("Trying to get a closed transaction")
        if (_tr.get() == null) {
            val tr = db.createTransaction()
            _tr.set(tr)
            //println("Starting transaction $this : $db : ${_tr.get()}")
        }
        return _tr.get()!!
    }

    override fun close() {
        val tr = _tr.get()
        //println("Closing $this : $db : $tr")
        tr?.commit()
        tr?.close()
        closed = true
    }
}

@KuickInternalWarning
open class ORMTableDefinition<T : Any> (
        val serializationStrategies : SerializationStrategy,
        val clazz: KClass<T>,
        val tableName: String = clazz.simpleName!!
): TableDefinition(tableName) {
    private val _map: MutableMap<KProperty1<T, *>, ColumnDefinition<*>> = mutableMapOf()

    @Deprecated("Do not use this, since this can be confused with Kotlin's to that generate pairs. Also an infix function SHOULD NOT mutate anything because that could be misleading and a source of bugs.", ReplaceWith("put(this, cd)"))
    infix fun <R: Any?> KProperty1<T, R>.to(cd: ColumnDefinition<R>) = put(this, cd)

    fun <R: Any?> put(prop: KProperty1<T, R>, cd: ColumnDefinition<R>) {
        _map[prop] = cd
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

    fun select(tr: DomainTransaction, predicate: () -> Expression<Boolean>): List<T> = select(tr.squashTr(), predicate, null)

    fun select(tr: DomainTransaction, a: AttributedModelQuery<T>?, predicate: () -> Expression<Boolean>): List<T> = select(tr.squashTr(), predicate, a)

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

    private fun select(tr: Transaction, predicate: (() -> Expression<Boolean>)? = null, a: AttributedModelQuery<T>? = null): List<T> =
            from(this).apply {
                predicate?.let { where(it) }
                val limit = a?.limit
                val orderByList = a?.orderBy?.list
                if (limit != null) {
                    limit(a.limit!!.toLong(), a.skip)
                }
                if (orderByList != null && orderByList.isNotEmpty()) {
                    for (orderBy in orderByList) {
                        orderBy(this@ORMTableDefinition[orderBy.prop], orderBy.ascending)
                    }
                }
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

    //=================================
    infix fun <T:Any> ResultRow.toDAO(ormDef: ORMTableDefinition<T>): T {
        val fields = ormDef.clazz.toDAOFields()
        val fieldValues = fields.map { f ->
            try {
                val property = ormDef.clazz.memberProperties.first { it.name == f.name }
                val columnDef = ormDef[property]
                val columnName = columnDef.name.id
                val tableName = ormDef.compoundName.id
                serializationStrategies.tryDecodeValueLazy(f.kotlinProperty!!.returnType) { clazz -> columnValue(clazz, columnName, tableName) }
            } catch (t: Throwable) {
                throw IllegalStateException("Had a problem reading field $f", t)
            }
        }
        return ormDef.clazz.constructors.first().call(*fieldValues.toTypedArray())
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

    private fun decodeValue(value: Any?): Any? = value?.let { serializationStrategies.tryEncodeValue(value) }

    //=================================

}

private fun <T:Any> KClass<T>.toDAOFields() = java.nonStaticFields()

@KuickInternalWarning
@Deprecated("")
inline fun <reified T> ResultRow.columnValue(columnName: String, tableName: String?  = null): T? =
        columnValue(T::class, columnName, tableName) as? T?

@KuickInternalWarning
inline fun <T> ignoreErrors(callback: () -> T): T? = runCatching { callback() }.getOrNull()

@KuickInternalWarning
fun DomainTransaction.squashTr() = (this as LazyDomainTransactionSquash).tr


@KuickInternalWarning
@Deprecated("")
infix fun <V: Any> ColumnDefinition<V>.eqEnum(literal: V?): Expression<Boolean> {

    return when {
        literal == null -> this.eq<V?>(null)
        literal is ColumnDefinition<*> -> this.eq(literal)
        else -> this.eq<V>(Json.toJson (literal) as V)
    }
}

@KuickInternalWarning
@Deprecated("")
infix fun <V : Any> ColumnDefinition<V>.withinEnum(literals: Collection<V>?): Expression<Boolean> {

    return when {
        literals == null -> literal(false)
        literals is ColumnDefinition<*> -> this.within(literals)
        else -> this.within<V>( literals.map { Json.toJson (it) as V })
    }
}

@KuickInternalWarning
@Deprecated("")
infix fun <V> ColumnDefinition<V>.eqId(literal: V?): Expression<Boolean> = when {
    literal == null -> this.eq<V?>(null)
    literal is ColumnDefinition<*> -> this.eq(literal)
    else -> this.eq<V>((literal as Id).id as V)
}

@KuickInternalWarning
@Deprecated("")
infix fun <V> ColumnDefinition<V>.eqLocalDate(literal: V?): Expression<Boolean> = when (literal) {
    null -> this.eq<V?>(null)
    else -> this.eq<V>((literal as LocalDate).toString() as V)
}

@KuickInternalWarning
@Deprecated("")
infix fun <V> ColumnDefinition<V>.gteqLocalDateTime(literal: V?): Expression<Boolean> = when (literal) {
    null -> this.gteq<V?>(null)
    else -> this.gteq<V>((literal as LocalDateTime).toString() as V)
}

@KuickInternalWarning
@Deprecated("")
infix fun <V> ColumnDefinition<V>.ltLocalDateTime(literal: V?): Expression<Boolean> = when (literal) {
    null -> this.lt<V?>(null)
    else -> this.lt<V>((literal as LocalDateTime).toString() as V)
}

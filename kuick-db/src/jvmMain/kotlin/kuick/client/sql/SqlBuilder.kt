package kuick.client.sql

import kuick.client.db.*
import kuick.orm.*
import kuick.repositories.*
import kotlin.reflect.*

abstract class SqlBuilder {
    companion object {
        val Iso = object : SqlBuilder() {}
    }

    protected fun String.quoteGeneric(quoteChar: Char): String = buildString {
        val base = this@quoteGeneric
        append(quoteChar)
        for (n in 0 until base.length) {
            val c = base[n]
            when (c) {
                '\'' -> append("\\\'")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(c)
            }
        }
        append(quoteChar)
    }

    open fun String.quoteTableName(): String = quoteGeneric('"')
    open fun String.quoteIdentifier(): String = quoteGeneric('"')
    open fun String.quoteStringLiteral(): String = quoteGeneric('\'')

    val String.qtable get() = quoteTableName()
    val String.qid get() = quoteIdentifier()
    val String.qstring get() = quoteStringLiteral()

    @JvmName("quoteTableNameExt") fun quoteTableName(str: String): String = str.quoteTableName()
    @JvmName("quoteStringLiteralExt") fun quoteStringLiteral(str: String): String = str.quoteStringLiteral()

    // Tables
    open fun sqlCreateTable(table: String, ifNotExists: Boolean = true): String = buildString {
        append("CREATE TABLE")
        if (ifNotExists) append(" IF NOT EXISTS")
        append(" ${table.quoteTableName()}()")
        append(";")
    }
    open fun sqlDropTable(table: String, ifExists: Boolean = true): String = buildString {
        append("DROP TABLE")
        if (ifExists) append(" IF EXISTS")
        append(" ${table.quoteTableName()}")
        append(";")
    }

    // Columns
    open fun sqlAddColumn(table: String, column: String, type: String, nullable: Boolean) = "ALTER TABLE ${table.quoteTableName()} ADD COLUMN ${column.quoteTableName()} $type ${if (nullable) "NULL" else "NOT NULL"};"
    open fun sqlDropColumn(table: String, column: String) = "ALTER TABLE ${table.quoteTableName()} DROP COLUMN ${column.quoteTableName()};"

    // Indices
    open fun sqlCreateIndex(table: String, columns: List<String>, unique: Boolean, index: String): String = buildString {
        append("CREATE ")
        if (unique) append("UNIQUE ")
        append("INDEX ").append(index.quoteTableName()).append(" ON ").append(table.quoteTableName())
        append(" (").append(columns.joinToString(", ") { it.quoteIdentifier() }).append(")")
    }
    open fun sqlDropIndex(table: String, index: String): String = "DROP INDEX ${index.quoteIdentifier()} ON ${table.quoteTableName()};"

    open fun sqlPlaceholders(count: Int, start: Int = 1) = (0 until count).joinToString(", ") { "?" }

    // Rows
    open fun sqlInsert(table: String, columns: List<String>): String = "INSERT INTO ${table.quoteTableName()} (${columns.joinToString(", ") { it.quoteTableName() }}) VALUES (${sqlPlaceholders(columns.size)});"
    open fun sqlDelete(table: String, condition: String): String = "DELETE FROM ${table.quoteTableName()} WHERE $condition;"

    // Describe
    open fun sqlListTables(): String = "SHOW TABLES;"
    open fun sqlListColumns(table: String): String = "SHOW COLUMNS FROM ${table.quoteTableName()};"

    // Types
    open fun typeVarchar(length: Int? = null) = if (length == null) "VARCHAR" else "VARCHAR($length)"
    open fun typeInt() = "INT"
    open fun typeTimestamp() = "TIMESTAMP"

    open fun Any?.quote() = when (this) {
        null -> "null"
        is Number -> "$this"
        else -> this.toString().quoteStringLiteral()
    }

    inner class OutParams(val start: Int = 1, val params: ArrayList<Any?> = arrayListOf()) {
        fun add(value: Any?): String {
            val placeholder = sqlPlaceholders(1, start + params.size)
            params.add(value)
            return placeholder
        }
    }

    fun outParams() = OutParams()

    open fun operator(op: String) = op

    @Suppress("UNCHECKED_CAST")
    open fun where(q: ModelQuery<*>, table: TableDefinition<*>, out: OutParams): String = when (q) {
        is SimpleFieldBinop<*, *> -> {
            //val vv = table.serialization.serialize((table as TableDefinition<Any>)[q.field as KProperty1<Any, *>], q.value)
            val placeholder = out.add(table.serialization.serialize((table as TableDefinition<Any>)[q.field as KProperty1<Any?, *>], q.value))
            //"${table.columnsByProp[q.field]!!.name.quoteIdentifier()} ${q.op} ${table.serialization.serialize((table as TableDefinition<Any>)[q.field as KProperty1<Any, *>], q.value).quote()}"
            "${table.columnsByProp[q.field]!!.name.quoteIdentifier()} ${operator(q.op)} $placeholder"
        }
        is FilterExpBinopLogic<*> -> "(${where(q.left, table, out)} ${operator(q.op)} ${where(q.right, table, out)})"
        is FilterExpUnopLogic<*> -> "${q.op} ${where(q.exp, table, out)}"
        is DecoratedModelQuery<*> -> where(q.base, table, out)
        else -> TODO("$q, $table")
    }

    open fun <T : Any> sqlSelect(q: ModelQuery<T>, table: TableDefinition<T>, out: OutParams = outParams()): QueryAndParams {
        val a = q.tryGetAttributed()
        val limit = a?.limit
        val offset = a?.skip
        val orderBy = a?.orderBy?.list
        return QueryAndParams(buildString {
            append("SELECT * FROM ${table.name.quoteTableName()}")
            append(" WHERE ")
            append(where(q, table, out))
            if (orderBy != null && orderBy.isNotEmpty()) {
                append(" ORDER BY")
                for (v in orderBy) {
                    append(" ${table[v.prop].name.quoteIdentifier()} ${if (v.ascending) "ASC" else "DESC"}")
                }
            }
            if (limit != null) append(" LIMIT $limit")
            if (offset != null) append(" OFFSET $offset")
            append(";")
        }, out.params)
    }

    open fun <T : Any> sqlDelete(q: ModelQuery<T>, table: TableDefinition<T>, out: OutParams = outParams()): QueryAndParams {
        return QueryAndParams(buildString {
            append("DELETE FROM ${table.name.quoteTableName()}")
            append(" WHERE ")
            append(where(q, table, out))
            append(";")
        }, out.params)
    }

    open fun <T : Any> sqlUpdate(keys: List<String>, q: ModelQuery<T>, table: TableDefinition<T>, out: OutParams = outParams()): QueryAndParams =
        sqlUpdateRaw(keys.map { "${it.qid} = ?" }, q, table, out)

    open fun <T : Any> sqlUpdateIncr(sets: List<String>, incrs: List<String>, q: ModelQuery<T>, table: TableDefinition<T>, out: OutParams = outParams()): QueryAndParams =
        sqlUpdateRaw(sets.map { "${it.qid} = ?" } + incrs.map { "${it.qid} = ${it.qid} + ?" }, q, table, out)

    open fun <T : Any> sqlUpdateRaw(raws: List<String>, q: ModelQuery<T>, table: TableDefinition<T>, out: OutParams = outParams()): QueryAndParams =
        QueryAndParams(buildString {
            append("UPDATE ${table.name.quoteTableName()}")
            append(" SET ")
            for ((index, raw) in raws.withIndex()) {
                if (index != 0) append(",")
                append(raw.replace("?", sqlPlaceholders(1, index + 1)))
            }
            append(" WHERE ")
            append(where(q, table, out))
            append(";")
        }, out.params)
}

object PgSqlBuilder : SqlBuilder() {
    override fun sqlListTables(): String = "SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema';"
    override fun sqlListColumns(table: String): String = "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_NAME = ${table.quoteStringLiteral()};"
    override fun sqlPlaceholders(count: Int, start: Int) = (0 until count).joinToString(", ") { "\$${it + start}" }
}
val SqlBuilder.Companion.Postgres get() = PgSqlBuilder

object H2SqlBuilder : SqlBuilder() {
    override fun sqlListTables(): String = "SELECT TABLE_NAME AS tablename FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA != 'INFORMATION_SCHEMA';"
    override fun operator(op: String) = when (op) {
        "~=" -> "LIKE"
        else -> op
    }
}
val SqlBuilder.Companion.H2 get() = H2SqlBuilder

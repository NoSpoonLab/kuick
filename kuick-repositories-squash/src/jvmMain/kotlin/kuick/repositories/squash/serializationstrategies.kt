package kuick.repositories.squash

import kuick.repositories.squash.orm.columnValue
import org.jetbrains.squash.definition.ColumnDefinition
import org.jetbrains.squash.definition.TableDefinition
import org.jetbrains.squash.definition.datetime
import org.jetbrains.squash.definition.long
import org.jetbrains.squash.results.ResultRow
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

interface DateSerializationStrategy {

    fun getColumnDefinition(table: TableDefinition, columnName: String) : ColumnDefinition<Any?>
    fun readColumnValue(result: ResultRow, columnName: String, tableName: String): Date?
    fun decodeValue(value: Date) : Any?
}

object dateSerializationAsDateTime : DateSerializationStrategy {
    override fun getColumnDefinition(table: TableDefinition, columnName: String) : ColumnDefinition<Any?> =
            table.datetime(columnName)

    override fun readColumnValue(result: ResultRow, columnName: String, tableName: String): Date? =
            result.columnValue<LocalDateTime>(columnName, tableName)?.let {
                val zoneOffset = ZoneOffset.UTC.normalized().rules.getOffset(it)
                Date.from(it.toInstant(zoneOffset))
            }

    override fun decodeValue(value: Date): Any? =
            LocalDateTime.ofInstant(value.toInstant(), ZoneOffset.UTC.normalized())
}

object dateSerializationAsLong : DateSerializationStrategy {
    override fun getColumnDefinition(table: TableDefinition, columnName: String) : ColumnDefinition<Any?> =
            table.long(columnName)

    override fun readColumnValue(result: ResultRow, columnName: String, tableName: String): Date? =
            result.columnValue<Long>(columnName, tableName)?.let { Date(it) }

    override fun decodeValue(value: Date): Any? = value.time
}
package kuick.client.orm

import kuick.client.db.*
import kuick.orm.*

fun <T : Any> DbPreparable.columnType(column: ColumnDefinition<T>): String {
    val columnType = column.columnType
    return when (columnType) {
        is ColumnType.INT -> sql.typeInt()
        is ColumnType.TIMESTAMP -> sql.typeTimestamp()
        is ColumnType.VARCHAR -> sql.typeVarchar(columnType.length)
        else -> sql.typeVarchar(column.maxLength)
    }
}

suspend fun <T : Any> DbPreparable.synchronizeTable(table: TableDefinition<T>) {
    val tableName = table.name
    createTable(tableName)
    val alreadyCreatedColumns = listColumns(tableName).toSet()
    for (column in table.columns) {
        if (column.name !in alreadyCreatedColumns) {
            addColumn(tableName, column.name, columnType(column), column.nullable)
        }
    }
    for (column in table.columns) {
        if (column.unique) {
            kotlin.runCatching {
                createIndex(tableName, listOf(column.name), unique = true)
            }
        }
    }
}

data class DbPreparableWithDefinitions(val preparable: DbPreparable, val definitions: TableDefinitions)

fun DbPreparable.withDefinitions(definitions: TableDefinitions) = DbPreparableWithDefinitions(this, definitions)

suspend fun <T : Any> DbPreparable.insert(table: TableDefinition<T>, instance: T): T = instance.also {
    insert(table.name, table.untype(instance))
}

suspend fun <T : Any> DbPreparable.query(table: TableDefinition<T>, query: QueryAndParams): List<T> =
        query(table, query.sql, *query.params.toTypedArray())
suspend fun <T : Any> DbPreparable.query(table: TableDefinition<T>, sql: String, vararg args: Any?): List<T> =
        query(sql, *args).map { table.type(it.map) }

suspend inline fun <reified T : Any> DbPreparableWithDefinitions.insert(instance: T) {
    val table = definitions.get<T>()
    preparable.insert(table, instance)
}

suspend inline fun <reified T : Any> DbPreparableWithDefinitions.query(sql: String, vararg args: Any?): List<T> {
    val table = definitions.get<T>()
    return preparable.query(table, sql, *args)
}

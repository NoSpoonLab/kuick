package kuick.client.orm

import kuick.client.db.*
import kuick.orm.*

fun <T : Any> DbPreparable.columnType(column: ColumnDefinition<T>): String {
    return when {
        Int::class == column.clazz -> sql.typeInt()
        else -> sql.typeVarchar()
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

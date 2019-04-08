package org.jetbrains.squash.schema

import org.jetbrains.squash.connection.*
import org.jetbrains.squash.definition.*
import org.jetbrains.squash.dialect.*

abstract class DatabaseSchemaBase(open val transaction: Transaction) : DatabaseSchema {

    override fun create(tables: List<TableDefinition>) {
        val statements = createStatements(tables)
        for (statement in statements) {
            transaction.executeStatement(statement)
        }
    }

    override fun createStatements(tables: List<TableDefinition>): List<SQLStatement> {
        val statements = ArrayList<SQLStatement>()
        if (tables.isEmpty())
            return statements

        val definition = transaction.connection.dialect.definition

        val existingTables = tables().toList()
        for (table in tables) {
            if (existingTables.any { it.name == table.compoundName.id })
                continue

            val tableDefinition = definition.tableSQL(table)
            statements.addAll(tableDefinition)
        }
        for (table in tables) {
            statements.addAll(definition.foreignKeys(table))
        }
        return statements
    }

    override fun validate(tables: List<Table>): List<DatabaseSchema.DatabaseSchemaValidationItem> {
        val tableMap = tables.associateBy { it.compoundName.id.toLowerCase() }
        val validationResult = mutableListOf<DatabaseSchema.DatabaseSchemaValidationItem>()
        transaction.databaseSchema().tables().forEach { tableSchema ->
            val tableDefinition = tableMap[tableSchema.name.toLowerCase()]
            if (tableDefinition == null)
                validationResult.add(DatabaseSchema.DatabaseSchemaValidationItem("Table definition not found for schema table '$tableSchema"))
            else {
                val columnsSchema = tableSchema.columns().associateBy { it.name.toLowerCase() }
                val columnsDefinition = tableDefinition.compoundColumns.associateBy { it.name.id.toLowerCase() }
                val allNames = columnsDefinition.keys + columnsSchema.keys
                for (name in allNames) {
                    val columnSchema = columnsSchema[name]
                    val columnDefinition = columnsDefinition[name]
                    when {
                        columnSchema == null -> validationResult.add(DatabaseSchema.DatabaseSchemaValidationItem("Column schema not found for definition column '$columnDefinition in table '$tableSchema'"))
                        columnDefinition == null -> validationResult.add(DatabaseSchema.DatabaseSchemaValidationItem("Column definition not found for schema column '$columnSchema' in table '$tableDefinition'"))
                        else -> {

                        }
                    }
                }
            }
        }
        return validationResult
    }
}
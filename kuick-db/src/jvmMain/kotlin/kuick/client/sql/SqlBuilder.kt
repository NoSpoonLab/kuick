package kuick.client.sql

abstract class SqlBuilder {
    object Iso : SqlBuilder()

    protected fun String.quoteGeneric(quoteChar: Char): String = buildString {
        val base = this@quoteGeneric
        append(quoteChar)
        for (n in 0 until base.length) {
            val c = base[n]
            when (c) {
                '\'' -> append("\\\'")
                '"' -> append("\\\"")
                else -> append(c)
            }
        }
        append(quoteChar)
    }

    open fun String.quoteTableName(): String = quoteGeneric('"')
    open fun String.quoteIdentifier(): String = quoteGeneric('"')
    open fun String.quoteStringLiteral(): String = quoteGeneric('\'')

    @JvmName("quoteTableNameExt") fun quoteTableName(str: String): String = str.quoteTableName()
    @JvmName("quoteStringLiteralExt") fun quoteStringLiteral(str: String): String = str.quoteStringLiteral()

    // Tables
    open fun sqlCreateTable(table: String, ifNotExists: Boolean = true): String = buildString {
        append("CREATE TABLE ${table.quoteTableName()}()")
        if (ifNotExists) append(" IF NOT EXISTS")
        append(";")
    }
    open fun sqlDropTable(table: String, ifExists: Boolean = true): String = buildString {
        append("DROP TABLE ${table.quoteTableName()}")
        if (ifExists) append(" IF EXISTS")
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
}

object PgSqlBuilder : SqlBuilder() {
    override fun sqlListTables(): String = "SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema';"
    override fun sqlListColumns(table: String): String = "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_NAME = ${table.quoteStringLiteral()};"
    override fun sqlPlaceholders(count: Int, start: Int) = (0 until count).joinToString(", ") { "\$${it + start}" }
}

object H2SqlBuilder : SqlBuilder() {
    override fun sqlListTables(): String = "SELECT TABLE_NAME AS tablename FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA != 'INFORMATION_SCHEMA';"
}
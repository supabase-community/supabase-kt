package io.github.jan.supacompose.realtime

class PostgresChangeBuilder(private val event: String) {

    var schema: String = ""
    var table: String? = null
    var filter: String? = null
    fun buildConfig() = PostgresJoinConfig(schema.ifBlank { throw IllegalStateException("Schema not set") }, table, filter, event)

}
package io.github.jan.supacompose.realtime

class PostgresChangeFilter(private val event: String) {

    /**
     * The schema name of the table that is being monitored. For normal supabase tables that might be "public".
     */

    var schema: String = ""

    /**
     * The table name that should be monitored
     */
    var table: String? = null

    /**
     * Filter the received changes in your table.
     * E.g.: "user_id=eq.1"
     */
    var filter: String? = null

    fun buildConfig() = PostgresJoinConfig(schema.ifBlank { throw IllegalStateException("The schema must be specified") }, table, filter, event)

}
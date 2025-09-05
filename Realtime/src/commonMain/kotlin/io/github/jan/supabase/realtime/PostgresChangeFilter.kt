package io.github.jan.supabase.realtime

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator

/**
 * Used to filter postgres changes
 */
class PostgresChangeFilter(private val event: String, private val schema: String) {

    /**
     * The table name that should be monitored
     */
    var table: String? = null

    /**
     * Filter the received changes in your table.
     * E.g.: "user_id=eq.1"
     */
    var filter: String? = null
        private set

    /**
     * Filters the received changes in your table.
     * @param filter The filter operation. Note that only some operators are supported. See [Postgres Changes](https://supabase.com/docs/guides/realtime/postgres-changes?language=kotlin#available-filters) for more information.
     */
    fun filter(filter: FilterOperation) {
        val filterValue = when(filter.operator) {
            FilterOperator.EQ,
            FilterOperator.NEQ,
            FilterOperator.GT,
            FilterOperator.GTE,
            FilterOperator.LT,
            FilterOperator.LTE ->
                escapeValue(filter.value)
            FilterOperator.IN -> {
                if(filter.value is List<*>) {
                    (filter.value as List<*>).joinToString(",", prefix = "(", postfix = ")") { escapeValue(it) }
                } else {
                    escapeValue(filter.value)
                }
            }
            else -> throw UnsupportedOperationException("Unsupported filter operator: ${filter.operator}")
        }
        this.filter = "${filter.column}=${filter.operator.name.lowercase()}.$filterValue"
    }

    /**
     * Filters the received changes in your table.
     * @param column The column name
     * @param operator The filter operator. Note that only some operators are supported. See [Postgres Changes](https://supabase.com/docs/guides/realtime/postgres-changes?language=kotlin#available-filters) for more information.
     * @param value The value to filter for. This can be context dependent. E.g. for the `IN` operator this can be a list of values, however you can also provide a String.
     */
    fun filter(column: String, operator: FilterOperator, value: Any) {
        filter(FilterOperation(column, operator, value))
    }

    @SupabaseInternal
    fun buildConfig() = PostgresJoinConfig(schema, table, filter, event)

}

private val quotedCharacters = listOf(",", ".", ":", "(", ")")

private fun escapeValue(value: Any?): String {
    val asString = value.toString()
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
    return if (quotedCharacters.any { asString.contains(it) }) {
        "\"$asString\""
    } else {
        asString
    }
}

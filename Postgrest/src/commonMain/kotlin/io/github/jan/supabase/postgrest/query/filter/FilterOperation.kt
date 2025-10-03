@file:Suppress(
    "UndocumentedPublicClass",
    "UndocumentedPublicFunction",
    "UndocumentedPublicProperty"
)

package io.github.jan.supabase.postgrest.query.filter

/**
 * Represents a filter operation for a column using a specific operator and a value.
 */
data class FilterOperation(val column: String, val operator: FilterOperator, val value: Any?)

fun FilterOperation.escapedValue(): String =
    when (operator) {
        FilterOperator.EQ,
        FilterOperator.NEQ,
        FilterOperator.GT,
        FilterOperator.GTE,
        FilterOperator.LT,
        FilterOperator.LTE,
        FilterOperator.LIKE,
        FilterOperator.ILIKE,
        FilterOperator.MATCH,
        FilterOperator.IMATCH,
        FilterOperator.IS ->
            escapeValue(value)

        FilterOperator.IN ->
            if (value is List<*>) {
                value.joinToString(",", prefix = "(", postfix = ")") { escapeValue(it) }
            } else {
                escapeValue(value)
            }
        FilterOperator.CS,
        FilterOperator.CD->
            if (value is List<*>) {
                value.joinToString(",", prefix = "{", postfix = "}") { escapeValue(it) }
            } else {
                escapeValue(value)
            }

        FilterOperator.OV ->
            when (value) {
                is List<*> -> value.joinToString(",", prefix = "{", postfix = "}") { escapeValue(it) }
                is Pair<*, *> -> "[${escapeValue(value.first)},${escapeValue(value.second)}]"
                else -> escapeValue(value)
            }

        FilterOperator.SL,
        FilterOperator.SR,
        FilterOperator.NXL,
        FilterOperator.NXR,
        FilterOperator.ADJ ->
            when (value) {
                is Pair<*, *> -> "(${escapeValue(value.first)},${escapeValue(value.second)})"
                is List<*> -> "(${escapeValue(value[0])},${escapeValue(value[1])})"
                else -> escapeValue(value)
            }

        else -> escapeValue(value) // Do these need special handling?
    }

private val quotedCharacters = listOf(",", ".", ":", "(", ")")

internal fun escapeValue(value: Any?): String {
    val asString = value.toString()
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
    return if (quotedCharacters.any { asString.contains(it) }) {
        "\"$asString\""
    } else {
        asString
    }
}

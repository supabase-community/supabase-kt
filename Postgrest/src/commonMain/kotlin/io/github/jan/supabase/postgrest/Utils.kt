package io.github.jan.supabase.postgrest

import io.github.jan.supabase.postgrest.query.PostgrestFilterBuilder
import io.github.jan.supabase.postgrest.query.buildPostgrestFilter
import kotlin.reflect.KProperty1

expect fun <T, V> getColumnName(property: KProperty1<T, V>): String
@PublishedApi internal inline fun PostgrestFilterBuilder.formatJoiningFilter(filter: PostgrestFilterBuilder.() -> Unit): String {
    val formattedFilter = buildPostgrestFilter(propertyConversionMethod, filter).toList().joinToString(",") {
        it.second.joinToString(",") { filter ->
            val isLogicalOperator = filter.startsWith("(") && filter.endsWith(")")
            it.first + (if(isLogicalOperator) "" else ".") + filter
        }
    }
    return "($formattedFilter)"
}

internal fun String.camelToSnakeCase(): String {
    val pattern = "(?<=.)[A-Z]".toRegex()
    return this.replace(pattern, "_$0").lowercase()
}
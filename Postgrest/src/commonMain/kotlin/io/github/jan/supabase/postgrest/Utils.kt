package io.github.jan.supabase.postgrest

import io.github.jan.supabase.postgrest.query.PostgrestFilterBuilder
import io.github.jan.supabase.postgrest.query.buildPostgrestFilter
import kotlin.reflect.KProperty1

private val SNAKE_CASE_REGEX = "(?<=.)[A-Z]".toRegex()

expect fun <T, V> getSerialName(property: KProperty1<T, V>): String

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
    return this.replace(SNAKE_CASE_REGEX, "_$0").lowercase()
}
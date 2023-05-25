package io.github.jan.supabase.postgrest

import io.github.jan.supabase.annotiations.SupabaseInternal
import io.github.jan.supabase.postgrest.query.PostgrestFilterBuilder
import io.github.jan.supabase.postgrest.query.buildPostgrestFilter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.KProperty1
import kotlin.reflect.typeOf

private val SNAKE_CASE_REGEX = "(?<=.)[A-Z]".toRegex()

@SupabaseInternal
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

@SupabaseInternal
internal fun String.camelToSnakeCase(): String {
    return this.replace(SNAKE_CASE_REGEX, "_$0").lowercase()
}

@OptIn(ExperimentalSerializationApi::class)
@SupabaseInternal
inline fun <reified T> classPropertyNames(): List<String> = serializerOrNull(typeOf<T>())?.descriptor?.elementNames?.toList() ?: throw IllegalArgumentException("Could not find serializer for ${T::class.simpleName}")

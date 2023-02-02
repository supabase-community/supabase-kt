package io.github.jan.supabase.postgrest.query

import io.github.jan.supabase.postgrest.getColumnName
import io.github.jan.supabase.supabaseJson
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.reflect.KProperty1

/**
 * Represents a postgrest update query
 */
class PostgrestUpdate {

    @PublishedApi
    internal val map = mutableMapOf<String, JsonElement>()

    inline infix fun <T, reified V> KProperty1<T, V>.setTo(value: V) {
        map[getColumnName(this)] = supabaseJson.encodeToJsonElement(value)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline infix fun <T> KProperty1<T, String>.setTo(value: String) = set(getColumnName(this), value)

    @Suppress("NOTHING_TO_INLINE")
    inline infix fun <T> KProperty1<T, Int>.setTo(value: Int) = set(getColumnName(this), value)

    @Suppress("NOTHING_TO_INLINE")
    inline infix fun <T> KProperty1<T, Long>.setTo(value: Long) = set(getColumnName(this), value)

    @Suppress("NOTHING_TO_INLINE")
    inline infix fun <T> KProperty1<T, Float>.setTo(value: Float) = set(getColumnName(this), value)

    @Suppress("NOTHING_TO_INLINE")
    inline infix fun <T> KProperty1<T, Double>.setTo(value: Double) = set(getColumnName(this), value)

    @Suppress("NOTHING_TO_INLINE")
    inline infix fun <T> KProperty1<T, Boolean>.setTo(value: Boolean) = set(getColumnName(this), value)

    operator fun set(column: String, value: String) {
        map[column] = JsonPrimitive(value)
    }

    operator fun set(column: String, value: Int) {
        map[column] = JsonPrimitive(value)
    }

    operator fun set(column: String, value: Long) {
        map[column] = JsonPrimitive(value)
    }

    operator fun set(column: String, value: Float) {
        map[column] = JsonPrimitive(value)
    }

    operator fun set(column: String, value: Double) {
        map[column] = JsonPrimitive(value)
    }

    operator fun set(column: String, value: Boolean) {
        map[column] = JsonPrimitive(value)
    }

    operator fun set(column: String, value: JsonElement) {
        map[column] = value
    }

    fun toJson() = JsonObject(map)

}

inline fun buildPostgrestUpdate(block: PostgrestUpdate.() -> Unit): JsonObject {
    val update = PostgrestUpdate()
    update.block()
    return update.toJson()
}
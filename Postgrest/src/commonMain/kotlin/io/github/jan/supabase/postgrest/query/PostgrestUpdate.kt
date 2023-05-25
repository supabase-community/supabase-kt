package io.github.jan.supabase.postgrest.query

import io.github.jan.supabase.annotiations.SupabaseInternal
import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.supabaseJson
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.reflect.KProperty1

/**
 * Represents a postgrest update query
 */
class PostgrestUpdate(@PublishedApi internal val propertyConversionMethod: PropertyConversionMethod) {

    @PublishedApi
    internal val map = mutableMapOf<String, JsonElement>()

    /**
     * Sets the value of the column with the name of the [KProperty1] to [value]
     */
    inline infix fun <T, reified V> KProperty1<T, V>.setTo(value: V?) {
        map[propertyConversionMethod(this)] = supabaseJson.encodeToJsonElement(value)
    }

    /**
     * Sets the value of the column with the name of the [KProperty1] to [value]
     */
    @Suppress("NOTHING_TO_INLINE")
    inline infix fun <T> KProperty1<T, String>.setTo(value: String?) = set(propertyConversionMethod(this), value)

    /**
     * Sets the value of the column with the name of the [KProperty1] to [value]
     */
    @Suppress("NOTHING_TO_INLINE")
    inline infix fun <T> KProperty1<T, Int>.setTo(value: Int?) = set(propertyConversionMethod(this), value)

    /**
     * Sets the value of the column with the name of the [KProperty1] to [value]
     */
    @Suppress("NOTHING_TO_INLINE")
    inline infix fun <T> KProperty1<T, Long>.setTo(value: Long?) = set(propertyConversionMethod(this), value)

    /**
     * Sets the value of the column with the name of the [KProperty1] to [value]
     */
    @Suppress("NOTHING_TO_INLINE")
    inline infix fun <T> KProperty1<T, Float>.setTo(value: Float?) = set(propertyConversionMethod(this), value)

    /**
     * Sets the value of the column with the name of the [KProperty1] to [value]
     */
    @Suppress("NOTHING_TO_INLINE")
    inline infix fun <T> KProperty1<T, Double>.setTo(value: Double?) = set(propertyConversionMethod(this), value)

    /**
     * Sets the value of the column with the name of the [KProperty1] to [value]
     */
    @Suppress("NOTHING_TO_INLINE")
    inline infix fun <T> KProperty1<T, Boolean>.setTo(value: Boolean?) = set(propertyConversionMethod(this), value)

    /**
     * Sets the value of the [column] to [value]
     */
    operator fun set(column: String, value: String?) {
        map[column] = JsonPrimitive(value)
    }

    /**
     * Sets the value of the [column] to [value]
     */
    operator fun set(column: String, value: Int?) {
        map[column] = JsonPrimitive(value)
    }

    /**
     * Sets the value of the [column] to [value]
     */
    operator fun set(column: String, value: Long?) {
        map[column] = JsonPrimitive(value)
    }

    /**
     * Sets the value of the [column] to [value]
     */
    operator fun set(column: String, value: Float?) {
        map[column] = JsonPrimitive(value)
    }

    /**
     * Sets the value of the [column] to [value]
     */
    operator fun set(column: String, value: Double?) {
        map[column] = JsonPrimitive(value)
    }

    /**
     * Sets the value of the [column] to [value]
     */
    operator fun set(column: String, value: Boolean?) {
        map[column] = JsonPrimitive(value)
    }

    /**
     * Sets the value of the [column] to [value]
     */
    operator fun set(column: String, value: JsonElement) {
        map[column] = value
    }

    @PublishedApi internal fun toJson() = JsonObject(map)

}

@SupabaseInternal
inline fun buildPostgrestUpdate(propertyConversionMethod: PropertyConversionMethod = PropertyConversionMethod.SERIAL_NAME, block: PostgrestUpdate.() -> Unit): JsonObject {
    val update = PostgrestUpdate(propertyConversionMethod)
    update.block()
    return update.toJson()
}
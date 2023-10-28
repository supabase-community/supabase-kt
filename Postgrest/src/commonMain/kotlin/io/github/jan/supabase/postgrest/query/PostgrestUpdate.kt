package io.github.jan.supabase.postgrest.query

import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.encodeToJsonElement
import io.github.jan.supabase.postgrest.PropertyConversionMethod
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.reflect.KProperty1

/**
 * Represents a postgrest update query
 */
class PostgrestUpdate(@PublishedApi internal val propertyConversionMethod: PropertyConversionMethod, @PublishedApi internal val serializer: SupabaseSerializer) {

    @PublishedApi
    internal val map = mutableMapOf<String, JsonElement>()

    /**
     * Sets the value of the column with the name of the [KProperty1] to [value]
     */
    inline infix fun <reified T, reified V> KProperty1<T, List<V>>.setTo(value: T) {
        if(value == null) {
            set(propertyConversionMethod(this), JsonNull)
        } else {
            set(propertyConversionMethod(this), serializer.encodeToJsonElement(value))
        }
    }

    /**
     * Sets the value of the column with the name of the [KProperty1] to [value]
     */
    infix fun <T> KProperty1<T, String>.setTo(value: String?) = set(propertyConversionMethod(this), value)

    /**
     * Sets the value of the column with the name of the [KProperty1] to [value]
     */
    infix fun <T> KProperty1<T, Int>.setTo(value: Int?) = set(propertyConversionMethod(this), value)

    /**
     * Sets the value of the column with the name of the [KProperty1] to [value]
     */
    infix fun <T> KProperty1<T, Long>.setTo(value: Long?) = set(propertyConversionMethod(this), value)

    /**
     * Sets the value of the column with the name of the [KProperty1] to [value]
     */
    infix fun <T> KProperty1<T, Float>.setTo(value: Float?) = set(propertyConversionMethod(this), value)

    /**
     * Sets the value of the column with the name of the [KProperty1] to [value]
     */
    infix fun <T> KProperty1<T, Double>.setTo(value: Double?) = set(propertyConversionMethod(this), value)

    /**
     * Sets the value of the column with the name of the [KProperty1] to [value]
     */
    infix fun <T> KProperty1<T, Boolean>.setTo(value: Boolean?) = set(propertyConversionMethod(this), value)

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
     * Sets the value of the [column] to null
     */
    fun setToNull(column: String) {
        map[column] = JsonNull
    }

    /**
     * Sets the value of the [column] to [value]
     */
    inline operator fun <reified T> set(column: String, value: T) {
        if(value == null) {
            map[column] = JsonNull
        } else {
            map[column] = serializer.encodeToJsonElement(value)
        }
    }

    @PublishedApi internal fun toJson() = JsonObject(map)

}

@SupabaseInternal
inline fun buildPostgrestUpdate(propertyConversionMethod: PropertyConversionMethod = PropertyConversionMethod.SERIAL_NAME, serializer: SupabaseSerializer, block: PostgrestUpdate.() -> Unit): JsonObject {
    val update = PostgrestUpdate(propertyConversionMethod, serializer)
    update.block()
    return update.toJson()
}
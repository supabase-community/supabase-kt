package io.github.jan.supacompose.postgrest.query

import io.github.jan.supacompose.postgrest.getColumnName
import io.github.jan.supacompose.supabaseJson
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.reflect.KProperty1

class PostgrestUpdate<T> {

    @PublishedApi
    internal val map = mutableMapOf<String, JsonElement>()

    inline infix fun <reified V> KProperty1<T, V>.setTo(value: V) {
        map[getColumnName(this)] = supabaseJson.encodeToJsonElement(value)
    }

    inline infix fun KProperty1<T, String>.setTo(value: String) = set(getColumnName(this), value)
    inline infix fun KProperty1<T, Int>.setTo(value: Int) = set(getColumnName(this), value)
    inline infix fun KProperty1<T, Long>.setTo(value: Long) = set(getColumnName(this), value)
    inline infix fun KProperty1<T, Float>.setTo(value: Float) = set(getColumnName(this), value)
    inline infix fun KProperty1<T, Double>.setTo(value: Double) = set(getColumnName(this), value)
    inline infix fun KProperty1<T, Boolean>.setTo(value: Boolean) = set(getColumnName(this), value)

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

}
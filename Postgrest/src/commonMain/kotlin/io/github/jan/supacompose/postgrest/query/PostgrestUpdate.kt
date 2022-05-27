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
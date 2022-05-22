package io.github.jan.supacompose

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

val supabaseJson = Json {
    ignoreUnknownKeys = true
}

fun String.toJsonObject(): JsonObject = supabaseJson.decodeFromString(this)

fun JsonObjectBuilder.putJsonObject(jsonObject: JsonObject) {
    for (key in jsonObject.keys) {
        put(key, jsonObject[key]!!)
    }
}
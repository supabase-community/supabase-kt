package io.github.jan.supacompose

import io.ktor.http.URLBuilder
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder

val supabaseJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = false
}

inline fun buildUrl(baseUrl: String, init: URLBuilder.() -> Unit): String {
    val builder = URLBuilder(baseUrl)
    builder.init()
    return builder.buildString()
}

fun String.toJsonObject(): JsonObject = supabaseJson.decodeFromString(this)

fun JsonObjectBuilder.putJsonObject(jsonObject: JsonObject) {
    for (key in jsonObject.keys) {
        put(key, jsonObject[key]!!)
    }
}
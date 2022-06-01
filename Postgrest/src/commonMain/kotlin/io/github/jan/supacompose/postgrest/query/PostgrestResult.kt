package io.github.jan.supacompose.postgrest.query

import io.github.jan.supacompose.supabaseJson
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

data class PostgrestResult(val body: String, val statusCode: Int) {

    /**
     * Decodes [body] as [T] using [json]
     */
    inline fun <reified T> decodeAs(json: Json = supabaseJson): T = json.decodeFromString(body)

    /**
     * Decodes [body] as [T] using [json]. If there's an error it will return null
     */
    inline fun <reified T> decodeAsOrNull(json: Json = supabaseJson): T? = try {
        decodeAs<T>(json)
    } catch (e: Exception) {
        null
    }

}

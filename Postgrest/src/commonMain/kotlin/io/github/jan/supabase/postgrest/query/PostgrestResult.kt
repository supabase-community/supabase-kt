package io.github.jan.supabase.postgrest.query

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * Represents the result from a postgrest request
 * @param body The body of the response. Can be decoded using [decodeAs] or [decodeAsOrNull]
 */
data class PostgrestResult(val body: JsonElement, val statusCode: Int) {

    /**
     * Decodes [body] as [T] using [json]
     */
    inline fun <reified T> decodeAs(json: Json = Json): T = json.decodeFromJsonElement(body)

    /**
     * Decodes [body] as [T] using [json]. If there's an error it will return null
     */
    inline fun <reified T> decodeAsOrNull(json: Json = Json): T? = try {
        decodeAs<T>(json)
    } catch (e: Exception) {
        null
    }

    /**
     * Decodes [body] as a list of [T], or an empty list if [body] couldn't be parsed as a list of [T]
     */
    inline fun <reified T> decodeList(json: Json = Json): List<T> = decodeAsOrNull(json) ?: emptyList()

    /**
     * Decodes [body] as a list of [T] and returns the first item found
     */
    inline fun <reified T> decodeSingle(json: Json = Json): T = decodeList<T>(json).first()

    /**
     * Decodes [body] as a list of [T] and returns the first item found or null
     */
    inline fun <reified T> decodeSingleOrNull(json: Json = Json): T? = decodeList<T>(json).firstOrNull()

}

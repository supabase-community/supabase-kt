package io.github.jan.supabase.postgrest.query

import io.github.jan.supabase.annotiations.SupabaseExperimental
import io.ktor.http.Headers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * Represents the result from a postgrest request
 * @param body The body of the response
 */
data class PostgrestResult(val body: JsonElement, val headers: Headers) {

    private val contentRange = headers["Content-Range"]

    /**
     * Returns the total amount of items in the database (null if no [Count] option was used in the request)
     */
    @SupabaseExperimental
    fun count(): Long? = contentRange?.substringAfter("/")?.toLongOrNull()

    /**
     * Returns the range of items returned
     */
    @SupabaseExperimental
    fun range(): LongRange? = contentRange?.substringBefore("/")?.let {
        val (start, end) = it.split("-")
        LongRange(start.toLong(), end.toLong())
    }

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
     * Decodes [body] as a list of [T]
     */
    inline fun <reified T> decodeList(json: Json = Json): List<T> = decodeAs(json)

    /**
     * Decodes [body] as a list of [T] and returns the first item found
     */
    inline fun <reified T> decodeSingle(json: Json = Json): T = decodeList<T>(json).first()

    /**
     * Decodes [body] as a list of [T] and returns the first item found or null
     */
    inline fun <reified T> decodeSingleOrNull(json: Json = Json): T? = decodeList<T>(json).firstOrNull()

}

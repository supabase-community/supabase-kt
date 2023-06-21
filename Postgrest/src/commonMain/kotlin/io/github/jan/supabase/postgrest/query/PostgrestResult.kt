package io.github.jan.supabase.postgrest.query

import io.github.jan.supabase.decode
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.http.Headers
import kotlinx.serialization.json.JsonElement

/**
 * Represents the result from a postgrest request
 * @param body The body of the response. Can be null if using an database function.
 * @param headers The headers of the response
 */
class PostgrestResult(val body: JsonElement?, val headers: Headers, @PublishedApi internal val postgrest: Postgrest) {

    private val contentRange = headers["Content-Range"]

    /**
     * Returns the total amount of items in the database (null if no [Count] option was used in the request)
     */
    fun count(): Long? = contentRange?.substringAfter("/")?.toLongOrNull()

    /**
     * Returns the range of items returned
     */
    fun range(): LongRange? = contentRange?.substringBefore("/")?.let {
        val (start, end) = it.split("-")
        LongRange(start.toLong(), end.toLong())
    }

    /**
     * Decodes [body] as [T] using [json]
     */
    inline fun <reified T : Any> decodeAs(): T = postgrest.config.serializer.decode(body?.toString() ?: error("No body found"))

    /**
     * Decodes [body] as [T] using [json]. If there's an error it will return null
     */
    inline fun <reified T : Any> decodeAsOrNull(): T? = try {
        decodeAs()
    } catch (e: Exception) {
        null
    }

    /**
     * Decodes [body] as a list of [T]
     */
    inline fun <reified T> decodeList(): List<T> = decodeAs()

    /**
     * Decodes [body] as a list of [T] and returns the first item found
     */
    inline fun <reified T> decodeSingle(): T = decodeList<T>().first()

    /**
     * Decodes [body] as a list of [T] and returns the first item found or null
     */
    inline fun <reified T> decodeSingleOrNull(): T? = decodeList<T>().firstOrNull()

}

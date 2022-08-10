package io.github.jan.supacompose.postgrest.query

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.auth
import io.github.jan.supacompose.exceptions.RestException
import io.github.jan.supacompose.postgrest.Postgrest
import io.github.jan.supacompose.supabaseJson
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.parametersOf
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlin.experimental.ExperimentalTypeInference

class PostgrestBuilder (val postgrest: Postgrest, val table: String) {

    @OptIn(ExperimentalTypeInference::class)
    suspend inline fun <reified T : Any> select(
        columns: String = "*",
        head: Boolean = false,
        count: Count? = null,
        @BuilderInference filter: PostgrestFilterBuilder.() -> Unit = {}
    ): PostgrestResult = postgrest.buildPostgrestRequest(table, if(head) HttpMethod.Head else HttpMethod.Get, prefer = buildList {
        if (count != null) {
            add("count=${count.identifier}")
        }
    }, filter = {
        filter()
        _params["select"] = columns.cleanColumns()
    })

    suspend inline fun <reified T : Any> insert(
        values: List<T>,
        upsert: Boolean = false,
        onConflict: String? = null,
        returning: Returning = Returning.REPRESENTATION,
        count: Count? = null,
        filter: PostgrestFilterBuilder.() -> Unit = {}
    ): PostgrestResult = postgrest.buildPostgrestRequest(table, HttpMethod.Post, supabaseJson.encodeToJsonElement(values), buildList {
        add("return=${returning.identifier}")
        if(upsert) add("resolution=merge-duplicates")
        if(count != null) add("count=${count.identifier}")
    }, filter = {
        filter()
        if (upsert && onConflict != null) _params["on_conflict"] = onConflict
    })

    suspend inline fun <reified T : Any> insert(
        value: T,
        upsert: Boolean = false,
        onConflict: String? = null,
        returning: Returning = Returning.REPRESENTATION,
        count: Count? = null,
        filter: PostgrestFilterBuilder.() -> Unit = {}
    ) = insert(listOf(value), upsert, onConflict, returning, count, filter)

    @OptIn(ExperimentalTypeInference::class)
    suspend inline fun update(
        crossinline update: PostgrestUpdate.() -> Unit = {},
        returning: Returning = Returning.REPRESENTATION,
        count: Count? = null,
        @BuilderInference filter: PostgrestFilterBuilder.() -> Unit = {}
    ): PostgrestResult = postgrest.buildPostgrestRequest(
        table,
        HttpMethod.Patch,
        JsonObject(PostgrestUpdate().apply(update).map),
        buildList {
            add("return=${returning.identifier}")
            if (count != null) add("count=${count.identifier}")
        },
        filter
    )

    @OptIn(ExperimentalTypeInference::class)
    suspend inline fun delete(
        returning: Returning = Returning.REPRESENTATION,
        count: Count? = null,
        @BuilderInference filter: PostgrestFilterBuilder.() -> Unit = {}
    ): PostgrestResult = postgrest.buildPostgrestRequest(table, HttpMethod.Delete, prefer = buildList {
        add("return=${returning.identifier}")
        if (count != null) add("count=${count.identifier}")
    }, filter = filter)

    @PublishedApi
    internal fun String.cleanColumns(): String {
        var quoted = false

        return this
            .toCharArray()
            .map { character ->
                if (character.isWhitespace() && !quoted) {
                    return@map ""
                }
                if (character == '"') {
                    quoted = !quoted
                }

                return@map character
            }.joinToString("")
    }

    companion object {
        const val HEADER_PREFER = "Prefer"
    }

}

suspend inline fun Postgrest.buildPostgrestRequest(
    table: String,
    method: HttpMethod,
    body: JsonElement? = null,
    prefer: List<String> = emptyList(),
    filter: PostgrestFilterBuilder.() -> Unit
) = supabaseClient.httpClient.request(
    resolveUrl(table)
) {
    this.method = method
    contentType(ContentType.Application.Json)
    headers[HttpHeaders.Authorization] = "Bearer ${supabaseClient.auth.currentSession.value?.accessToken ?: throw IllegalStateException("Trying to access database without a user session")}"
    headers[PostgrestBuilder.HEADER_PREFER] = prefer.joinToString(",")
    setBody(body)
    addPostgresFilter(filter)
}.checkForErrorCodes()

@PublishedApi
internal suspend fun HttpResponse.checkForErrorCodes(): PostgrestResult {
    if(status.value !in 200..299) {
        try {
            val error = body<JsonObject>()
            throw RestException(status.value, error["error"]?.jsonPrimitive?.content ?: "Unknown error", error.toString())
        } catch(_: Exception) {
            throw RestException(status.value, "Unknown error", "")
        }
    }
    return PostgrestResult(body(), status.value)
}

enum class Count(val identifier: String) {
    EXACT("exact"),
    PLANNED("planned"),
    ESTIMATED("estimated")
}

enum class Returning(val identifier: String) {
    MINIMAL("minimal"),
    REPRESENTATION("representation")
}
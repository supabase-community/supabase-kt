package io.github.jan.supacompose.postgrest.query

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.auth
import io.github.jan.supacompose.supabaseJson
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.serialization.encodeToString
import kotlin.experimental.ExperimentalTypeInference

class PostgrestBuilder (val supabaseClient: SupabaseClient, val table: String) {

    @OptIn(ExperimentalTypeInference::class)
    suspend inline fun <reified T : Any> select(
        columns: String = "*",
        head: Boolean = false,
        count: Count? = null,
        @BuilderInference filter: PostgrestFilterBuilder<T>.() -> Unit = {}
    ): PostgrestResult = supabaseClient.buildPostgrestRequest<T>(table, if(head) HttpMethod.Head else HttpMethod.Get, prefer = buildList {
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
        filter: PostgrestFilterBuilder<T>.() -> Unit = {}
    ): PostgrestResult = supabaseClient.buildPostgrestRequest<T>(table, HttpMethod.Post, supabaseJson.encodeToString(values), buildList {
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
        filter: PostgrestFilterBuilder<T>.() -> Unit = {}
    ) = insert(listOf(value), upsert, onConflict, returning, count, filter)

    @OptIn(ExperimentalTypeInference::class)
    suspend inline fun <reified T : Any> update(
        crossinline update: PostgrestUpdate<T>.() -> Unit = {},
        returning: Returning = Returning.REPRESENTATION,
        count: Count? = null,
        @BuilderInference filter: PostgrestFilterBuilder<T>.() -> Unit = {}
    ): PostgrestResult = supabaseClient.buildPostgrestRequest<T>(
        table,
        HttpMethod.Patch,
        supabaseJson.encodeToString(PostgrestUpdate<T>().apply(update).map),
        buildList {
            add("return=${returning.identifier}")
            if (count != null) add("count=${count.identifier}")
        },
        filter
    )

    @OptIn(ExperimentalTypeInference::class)
    suspend inline fun <reified T : Any> delete(
        returning: Returning = Returning.REPRESENTATION,
        count: Count? = null,
        @BuilderInference filter: PostgrestFilterBuilder<T>.() -> Unit = {}
    ): PostgrestResult = supabaseClient.buildPostgrestRequest(table, HttpMethod.Delete, prefer = buildList {
        add("return=${returning.identifier}")
        if (count != null) add("count=${count.identifier}")
    }, filter = filter)

    companion object {
        const val HEADER_PREFER = "Prefer"
    }

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

}

suspend inline fun <T : Any> SupabaseClient.buildPostgrestRequest(
    table: String,
    method: HttpMethod,
    body: String? = null,
    prefer: List<String> = emptyList(),
    filter: PostgrestFilterBuilder<T>.() -> Unit
) = httpClient.request(
    "$supabaseHttpUrl/rest/v1/$table"
) {
    this.method = method
    contentType(ContentType.Application.Json)
    headers[HttpHeaders.Authorization] = "Bearer ${auth.currentSession.value?.accessToken ?: throw IllegalStateException("Trying to access database without a user session")}"
    headers[PostgrestBuilder.HEADER_PREFER] = prefer.joinToString(",")
    setBody(body)
    PostgrestFilterBuilder<T>().apply(filter).params.forEach {
        parameter(it.key, it.value)
    }
}.let {
    PostgrestResult(it.body(), it.status.value)
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
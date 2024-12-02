package io.supabase.postgrest.executor

import io.supabase.postgrest.Postgrest
import io.supabase.postgrest.query.PostgrestQueryBuilder
import io.supabase.postgrest.query.Returning
import io.supabase.postgrest.request.PostgrestRequest
import io.supabase.postgrest.result.PostgrestResult
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.parametersOf

internal fun HttpRequestBuilder.configurePostgrestRequest(
    request: PostgrestRequest
) {
    method = request.method
    contentType(ContentType.Application.Json)
    headers.appendAll(request.headers)
    headers[PostgrestQueryBuilder.Companion.HEADER_PREFER] = request.prefer.joinToString(",")
    with(url.parameters) {
        appendAll(parametersOf(request.urlParams.mapValues { (_, value) -> listOf(value) }))
        if (request.returning is Returning.Representation) {
            append("select", (request.returning as Returning.Representation).columns.value)
        }
    }
    request.body?.let { setBody(it) }
    if (request.schema.isNotBlank()) {
        when (method) {
            HttpMethod.Get, HttpMethod.Head -> header("Accept-Profile", request.schema)
            else -> header("Content-Profile", request.schema)
        }
    }
}

internal suspend fun HttpResponse.asPostgrestResult(postgrest: Postgrest): PostgrestResult =
    PostgrestResult(bodyAsText(), headers, postgrest)
package io.github.jan.supabase.postgrest.executor

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import io.github.jan.supabase.postgrest.query.Returning
import io.github.jan.supabase.postgrest.request.PostgrestRequest
import io.github.jan.supabase.postgrest.result.PostgrestResult
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
    method = request.httpOptions.method
    contentType(ContentType.Application.Json)
    headers.appendAll(request.headerOptions.headers)
    headers[PostgrestQueryBuilder.HEADER_PREFER] = request.headerOptions.prefer.joinToString(",")
    if (request.headerOptions.stripNulls) {
        val currentAccept = this.headers["Accept"]
        if (currentAccept == "application/vnd.pgrst.object+json") {
            this.headers["Accept"] = "application/vnd.pgrst.object+json;nulls=stripped"
        } else if (currentAccept == null || currentAccept == "application/json") {
            this.headers["Accept"] = "application/vnd.pgrst.array+json;nulls=stripped"
        }
    }
    with(url.parameters) {
        appendAll(parametersOf(request.urlParamOptions.urlParams.mapValues { (_, value) -> listOf(value) }))
        val returning = request.urlParamOptions.returning
        if (returning is Returning.Representation) {
            append("select", (returning as Returning.Representation).columns.value)
        }
    }
    request.httpOptions.body?.let { setBody(it) }
    if (request.headerOptions.schema.isNotBlank()) {
        when (method) {
            HttpMethod.Get, HttpMethod.Head -> header("Accept-Profile", request.headerOptions.schema)
            else -> header("Content-Profile", request.headerOptions.schema)
        }
    }
}

internal suspend fun HttpResponse.asPostgrestResult(postgrest: Postgrest): PostgrestResult =
    PostgrestResult(bodyAsText(), headers, postgrest)
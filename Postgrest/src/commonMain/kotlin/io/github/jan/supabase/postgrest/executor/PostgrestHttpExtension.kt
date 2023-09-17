package io.github.jan.supabase.postgrest.executor

import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.PostgrestBuilder
import io.github.jan.supabase.postgrest.request.PostgrestRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.parametersOf

internal fun HttpRequestBuilder.configurePostgrestRequest(
    request: PostgrestRequest,
    postgrest: Postgrest
) {
    method = request.method
    contentType(ContentType.Application.Json)
    val token = postgrest.config.jwtToken
        ?: postgrest.supabaseClient.pluginManager.getPluginOrNull(GoTrue)
            ?.currentAccessTokenOrNull()
    token?.let {
        bearerAuth(it)
    }
    if (request.single) {
        accept(ContentType(HttpHeaders.Accept, "application/vnd.pgrst.object+json"))
    }
    headers[PostgrestBuilder.HEADER_PREFER] = request.prefer.joinToString(",")
    with(url.parameters) {
        appendAll(parametersOf(request.urlParams.mapValues { (_, value) -> listOf(value) }))
        appendAll(parametersOf(request.filter.mapValues { (_, value) -> listOf(value.first()) }))
    }
    if (request.schema.isNotBlank()) {
        when (method) {
            HttpMethod.Get, HttpMethod.Head -> header("Accept-Profile", request.schema)
            else -> header("Content-Profile", request.schema)
        }
    }
}
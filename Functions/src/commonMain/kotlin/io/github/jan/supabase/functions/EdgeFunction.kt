package io.github.jan.supabase.functions

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders

/**
 * Represents a reusable edge function. Can be created using [Functions.buildEdgeFunction]
 * @param functionName The name of the function
 * @param headers Headers to add to the request
 * @param supabaseClient The supabase client to use
 */
class EdgeFunction @SupabaseInternal constructor(
    val functionName: String,
    val headers: Headers,
    val supabaseClient: SupabaseClient
) {

    /**
     * Invokes the edge function
     * Note, if you want to serialize [body] to json, you need to add the [HttpHeaders.ContentType] header yourself.
     * @param body The body to send
     * @param headerOverride Overrides [headers] and adds additional headers
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline operator fun <reified T> invoke(body: T, headerOverride: HeadersBuilder.() -> Unit = {}): HttpResponse {
        val headers = HeadersBuilder().apply {
            appendAll(this@EdgeFunction.headers)
            headerOverride()
        }
        return supabaseClient.functions.invoke(functionName, body, headers.build())
    }

    /**
     * Invokes the edge function
     * @param headerOverride Overrides [headers] and adds additional headers
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline operator fun invoke(headerOverride: HeadersBuilder.() -> Unit = {}): HttpResponse {
        val headers = HeadersBuilder().apply {
            appendAll(this@EdgeFunction.headers)
            headerOverride()
        }
        return supabaseClient.functions.invoke(function = functionName, headers = headers.build())
    }
}
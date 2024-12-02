package io.supabase.functions

import io.supabase.SupabaseClient
import io.supabase.annotations.SupabaseInternal
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
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
     * @param requestOverride Overrides the HTTP request
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline operator fun <reified T : Any> invoke(body: T, crossinline requestOverride: HttpRequestBuilder.() -> Unit = {}): HttpResponse {
        return supabaseClient.functions.invoke(functionName) {
            headers.appendAll(this@EdgeFunction.headers)
            requestOverride()
            setBody(body)
        }
    }

    /**
     * Invokes the edge function
     * @param requestOverride Overrides the HTTP request
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline operator fun invoke(crossinline requestOverride: HttpRequestBuilder.() -> Unit = {}): HttpResponse {
        return supabaseClient.functions.invoke(functionName) {
            headers.appendAll(this@EdgeFunction.headers)
            requestOverride()
        }
    }
}
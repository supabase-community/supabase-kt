package io.github.jan.supabase.functions

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.ByteReadChannel

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

    /**
     * Invokes the edge function and returns the response body as a [ByteReadChannel], suitable for
     * streaming responses such as Server-Sent Events (SSE) or chunked transfer encoding.
     *
     * For line-by-line text streaming (e.g. SSE), use the [asFlow] extension function on the returned channel.
     * @param requestOverride Overrides the HTTP request
     * @return A [ByteReadChannel] for reading the response body incrementally
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun invokeStreaming(crossinline requestOverride: HttpRequestBuilder.() -> Unit = {}): ByteReadChannel {
        return supabaseClient.functions.invokeStreaming(functionName) {
            headers.appendAll(this@EdgeFunction.headers)
            requestOverride()
        }
    }

    /**
     * Prepares an invocation of the edge function, returning an [HttpStatement] for streaming
     * or other advanced response handling.
     * @param requestOverride Overrides the HTTP request
     */
    suspend inline fun prepareInvoke(crossinline requestOverride: HttpRequestBuilder.() -> Unit = {}): HttpStatement {
        return supabaseClient.functions.prepareInvoke(functionName) {
            headers.appendAll(this@EdgeFunction.headers)
            requestOverride()
        }
    }
}
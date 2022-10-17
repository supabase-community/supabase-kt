package io.github.jan.supabase.functions

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotiations.SupaComposeInternal
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders

/**
 * Represents a reusable edge function. Can be created using [Functions.buildEdgeFunction]
 * @param functionName The name of the function
 * @param headers Headers to add to the request
 * @param supabaseClient The supabase client to use
 */
class EdgeFunction @SupaComposeInternal constructor(
    val functionName: String,
    val headers: Headers,
    val supabaseClient: SupabaseClient
) {

    /**
     * Invokes the edge function
     * Note, if you want to serialize [body] to json, you need to add the [HttpHeaders.ContentType] header yourself.
     */
    suspend inline operator fun <reified T> invoke(body: T) = supabaseClient.functions.invoke(functionName, body, headers)

    /**
     * Invokes the edge function
     */
    suspend inline operator fun invoke() = supabaseClient.functions.invoke(function = functionName, headers = headers)

}

class EdgeFunctionBuilder(var functionName: String = "", val headers: HeadersBuilder = HeadersBuilder(), private val supabaseClient: SupabaseClient) {

    @OptIn(SupaComposeInternal::class)
    fun toEdgeFunction() = EdgeFunction(functionName, headers.build(), supabaseClient)

}
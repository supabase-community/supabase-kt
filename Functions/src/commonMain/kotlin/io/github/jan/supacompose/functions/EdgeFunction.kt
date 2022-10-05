package io.github.jan.supacompose.functions

import io.github.jan.supacompose.SupabaseClient
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders

class EdgeFunction(
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

    fun toEdgeFunction() = EdgeFunction(functionName, headers.build(), supabaseClient)

}
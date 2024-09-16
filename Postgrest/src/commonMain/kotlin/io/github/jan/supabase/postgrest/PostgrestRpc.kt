@file:Suppress("MatchingDeclarationName")
package io.github.jan.supabase.postgrest

import io.github.jan.supabase.encodeToJsonElement
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.query.request.RpcPostgrestRequestBuilder
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.jsonObject

/**
 * Enum class for the different HTTP methods that can be used in a RPC request
 * @property httpMethod The HTTP method
 */
enum class RpcMethod(val httpMethod: HttpMethod) {
    /**
     * HTTP HEAD method. If used, no body will be returned. Useful when using count. The parameters will be sent as query parameters.
     */
    HEAD(HttpMethod.Head),
    /**
     * HTTP POST method. Default method.
     */
    POST(HttpMethod.Post),
    /**
     * HTTP GET method. If used, the function will be called in read-only mode. The parameters will be sent as query parameters.
     */
    GET(HttpMethod.Get);
}

/**
 * Executes a database function
 *
 * @param function The name of the function
 * @param parameters The parameters for the function
 * @param request Filter the result
 * @throws RestException or one of its subclasses if the request failed
 */
suspend inline fun <reified T : Any> Postgrest.rpc(
    function: String,
    parameters: T,
    noinline request: RpcPostgrestRequestBuilder.() -> Unit = {},
): PostgrestResult = rpc(function, serializer.encodeToJsonElement(parameters).jsonObject, request)
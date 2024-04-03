package io.github.jan.supabase.postgrest

import io.github.jan.supabase.encodeToJsonElement
import io.github.jan.supabase.postgrest.executor.RestRequestExecutor
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder
import io.github.jan.supabase.postgrest.request.RpcRequest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonElement
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
 * @param method The HTTP method to use. Default is POST
 * @param request Filter the result
 * @throws RestException or one of its subclasses if the request failed
 */
suspend inline fun <reified T : Any> Postgrest.rpc(
    function: String,
    parameters: T,
    method: RpcMethod = RpcMethod.POST,
    request: PostgrestRequestBuilder.() -> Unit = {},
): PostgrestResult {
    val encodedParameters = if (parameters is JsonElement) parameters else serializer.encodeToJsonElement(parameters)
    val requestBuilder = PostgrestRequestBuilder(config.propertyConversionMethod).apply(request)
    val urlParams = buildMap {
        putAll(requestBuilder.params.mapToFirstValue())
        if(method != RpcMethod.POST) {
            putAll(encodedParameters.jsonObject.mapValues { it.value.toString() })
        }
    }
    val rpcRequest = RpcRequest(
        method = method.httpMethod,
        count = requestBuilder.count,
        urlParams = urlParams,
        body = encodedParameters
    )
    return RestRequestExecutor.execute(postgrest = this, path = "rpc/$function", request = rpcRequest)
}

/**
 * Executes a database function
 *
 * @param function The name of the function
 * @param method The HTTP method to use. Default is POST
 * @param request Filter the result
 * @throws RestException or one of its subclasses if the request failed
 */
suspend inline fun Postgrest.rpc(
    function: String,
    method: RpcMethod = RpcMethod.POST,
    request: PostgrestRequestBuilder.() -> Unit = {}
): PostgrestResult {
    val requestBuilder = PostgrestRequestBuilder(config.propertyConversionMethod).apply(request)
    val rpcRequest = RpcRequest(
        method = method.httpMethod,
        count = requestBuilder.count,
        urlParams = requestBuilder.params.mapToFirstValue()
    )
    return RestRequestExecutor.execute(postgrest = this, path = "rpc/$function", request = rpcRequest)
}
package io.github.jan.supabase.postgrest

import io.github.jan.supabase.encodeToJsonElement
import io.github.jan.supabase.postgrest.executor.RestRequestExecutor
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.PostgrestFilterBuilder
import io.github.jan.supabase.postgrest.request.RpcRequest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.serialization.json.JsonElement


/**
 * Executes a database function
 *
 * @param function The name of the function
 * @param parameters The parameters for the function
 * @param head If true, select will delete the selected data.
 * @param count Count algorithm to use to count rows in a table.
 * @param filter Filter the result
 * @throws RestException or one of its subclasses if the request failed
 */
suspend inline fun <reified T : Any> Postgrest.rpc(
    function: String,
    parameters: T,
    head: Boolean = false,
    count: Count? = null,
    filter: PostgrestFilterBuilder.() -> Unit = {},
): PostgrestResult {
    val encodedParameters = if (parameters is JsonElement) parameters else serializer.encodeToJsonElement(parameters)
    val rpcRequest = RpcRequest(
        head = head,
        count = count,
        filter = PostgrestFilterBuilder(config.propertyConversionMethod).apply(filter).params,
        body = encodedParameters
    )
    return RestRequestExecutor.execute(postgrest = this, path = "rpc/$function", request = rpcRequest)
}

/**
 * Executes a database function
 *
 * @param function The name of the function
 * @param head If true, select will delete the selected data.
 * @param count Count algorithm to use to count rows in a table.
 * @param filter Filter the result
 * @throws RestException or one of its subclasses if the request failed
 */
suspend inline fun Postgrest.rpc(
    function: String,
    head: Boolean = false,
    count: Count? = null,
    filter: PostgrestFilterBuilder.() -> Unit = {}
): PostgrestResult {
    val rpcRequest = RpcRequest(
        head = head,
        count = count,
        filter = PostgrestFilterBuilder(config.propertyConversionMethod).apply(filter).params
    )
    return RestRequestExecutor.execute(postgrest = this, path = "rpc/$function", request = rpcRequest)
}
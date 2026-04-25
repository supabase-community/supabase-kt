package io.github.jan.supabase.postgrest.query.request

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.postgrest.RpcMethod
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder
import io.ktor.http.HttpMethod

/**
 * Request builder for [Postgrest.rpc]
 */
class RpcRequestBuilder(defaultSchema: String, propertyConversionMethod: PropertyConversionMethod): PostgrestRequestBuilder(
    defaultSchema,
    propertyConversionMethod
) {

    var method: RpcMethod = RpcMethod.POST
        set(value) {
            httpMethod = value.httpMethod
            field = value
        }

    init {
        httpMethod = HttpMethod.Post
    }

    override fun buildPrefer(): List<String> {
        return if (count != null) listOf("count=${count!!.identifier}") else listOf()
    }

}
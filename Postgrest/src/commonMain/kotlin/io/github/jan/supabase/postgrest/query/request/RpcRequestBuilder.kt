package io.github.jan.supabase.postgrest.query.request

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.postgrest.RpcMethod
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder
import io.github.jan.supabase.postgrest.rpcRequest

/**
 * Request builder for [Postgrest.rpcRequest]
 */
class RpcRequestBuilder(defaultSchema: String, propertyConversionMethod: PropertyConversionMethod): PostgrestRequestBuilder(propertyConversionMethod) {

    /**
     * The HTTP method to use. Default is POST
     */
    var method: RpcMethod = RpcMethod.POST

    /**
     * The database schema
     */
    var schema: String = defaultSchema

}
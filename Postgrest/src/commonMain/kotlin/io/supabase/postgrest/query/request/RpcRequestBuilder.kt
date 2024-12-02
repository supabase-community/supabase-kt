package io.supabase.postgrest.query.request

import io.supabase.postgrest.PropertyConversionMethod
import io.supabase.postgrest.RpcMethod
import io.supabase.postgrest.query.PostgrestRequestBuilder

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
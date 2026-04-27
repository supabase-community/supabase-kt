package io.github.jan.supabase.postgrest.query.request

import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder
import io.ktor.http.HttpMethod

/**
 * Request builder for [PostgrestQueryBuilder.select]
 */
class SelectRequestBuilder(defaultSchema: String, propertyConversionMethod: PropertyConversionMethod): PostgrestRequestBuilder(
    defaultSchema,
    propertyConversionMethod
) {

    /**
     * If true, no body will be returned. Useful when using count.
     */
    var head: Boolean = false
        set(value) {
            httpMethod = if (value) HttpMethod.Head else HttpMethod.Get
            field = value
        }

}
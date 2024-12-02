package io.supabase.postgrest.query.request

import io.supabase.postgrest.PropertyConversionMethod
import io.supabase.postgrest.query.PostgrestQueryBuilder
import io.supabase.postgrest.query.PostgrestRequestBuilder

/**
 * Request builder for [PostgrestQueryBuilder.select]
 */
class SelectRequestBuilder(propertyConversionMethod: PropertyConversionMethod): PostgrestRequestBuilder(propertyConversionMethod) {

    /**
     * If true, no body will be returned. Useful when using count.
     */
    var head: Boolean = false

}
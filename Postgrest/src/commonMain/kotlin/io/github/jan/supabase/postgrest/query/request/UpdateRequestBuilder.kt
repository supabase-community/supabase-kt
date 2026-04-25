package io.github.jan.supabase.postgrest.query.request

import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder

/**
 * Request builder for [PostgrestQueryBuilder.update]
 */
class UpdateRequestBuilder(defaultSchema: String, propertyConversionMethod: PropertyConversionMethod): PostgrestRequestBuilder(
    defaultSchema,
    propertyConversionMethod
) {

    override fun buildPrefer(): List<String> {
        return buildList {
            add("return=${returning.identifier}")
            if (count != null) add("count=${count!!.identifier}")
        }
    }

}
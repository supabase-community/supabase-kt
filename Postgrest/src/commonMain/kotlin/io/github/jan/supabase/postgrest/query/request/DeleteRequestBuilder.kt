package io.github.jan.supabase.postgrest.query.request

import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder
import io.ktor.http.HttpMethod

/**
 * Request builder for [PostgrestQueryBuilder.delete]
 */
class DeleteRequestBuilder(defaultSchema: String, propertyConversionMethod: PropertyConversionMethod): PostgrestRequestBuilder(
    defaultSchema,
    propertyConversionMethod
) {

    init {
        httpMethod = HttpMethod.Delete
    }

    override fun buildPrefer(): List<String> {
        return buildList {
            add("return=${returning.identifier}")
            if (count != null) add("count=${count!!.identifier}")
        }
    }

}
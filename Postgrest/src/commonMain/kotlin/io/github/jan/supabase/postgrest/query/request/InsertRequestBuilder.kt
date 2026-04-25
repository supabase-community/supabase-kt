package io.github.jan.supabase.postgrest.query.request

import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder
import io.ktor.http.HttpMethod

/**
 * Request builder for [PostgrestQueryBuilder.insert]
 */
open class InsertRequestBuilder(defaultSchema: String, propertyConversionMethod: PropertyConversionMethod): PostgrestRequestBuilder(
    defaultSchema,
    propertyConversionMethod
) {

    /**
     * Make missing fields default to `null`.
     * Otherwise, use the default value for the column. This only applies when
     * inserting new rows, not when merging with existing rows under
     */
    var defaultToNull: Boolean = true

    init {
        httpMethod = HttpMethod.Post
    }

    protected fun buildPrefer(
        upsert: Boolean,
        ignoreDuplicates: Boolean,
    ): List<String> {
        return buildList {
            add("return=${returning.identifier}")
            if (upsert) add("resolution=${if (ignoreDuplicates) "ignore" else "merge"}-duplicates")
            if(!defaultToNull) add("missing=default")
            if (count != null) add("count=${count!!.identifier}")
        }
    }

    override fun buildPrefer(): List<String> {
        return buildPrefer(upsert = false, ignoreDuplicates = false)
    }

}
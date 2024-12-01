package io.supabase.postgrest.query.request

import io.supabase.postgrest.PropertyConversionMethod
import io.supabase.postgrest.query.PostgrestQueryBuilder

/**
 * Request builder for [PostgrestQueryBuilder.upsert]
 */
class UpsertRequestBuilder(propertyConversionMethod: PropertyConversionMethod): InsertRequestBuilder(propertyConversionMethod) {

    /**
     * Comma-separated UNIQUE column(s) to specify how
     * duplicate rows are determined. Two rows are duplicates if all the
     * `onConflict` columns are equal.
     */
    var onConflict: String? = null

    /**
     * If `true`, duplicate rows are ignored. If `false`, duplicate rows are merged with existing rows.
     */
    var ignoreDuplicates: Boolean = false

}
package io.supabase.storage

import io.supabase.annotations.SupabaseInternal
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * A filter builder for [BucketApi.list]
 */
class BucketListFilter {

    /**
     * The limit of items to return
     */
    var limit: Int? = null

    /**
     * The starting position
     */
    var offset: Int? = null

    /**
     * The search string to filter files by.
     */
    var search: String? = null
    private var column: String? = null
    private var order: String? = null

    /**
     * Sorts the result by the given [column] in the given [order]
     */
    fun sortBy(column: String, order: String) {
        this.column = column
        this.order = order
    }

    @SupabaseInternal
    fun build() = buildJsonObject {
        limit?.let {
            put("limit", it)
        }
        offset?.let {
            put("offset", it)
        }
        search?.let {
            put("search", it)
        }
        column?.let {
            putJsonObject("sortBy") {
                put("column", column)
                put("order", order)
            }
        }
    }

}
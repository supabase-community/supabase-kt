package io.github.jan.supabase.storage

import io.ktor.http.parameters

/**
 * A filter builder for [Storage.listBuckets]
 */
class BucketFilter {

    /**
     * The maximum number of buckets to return. If null, no limit is applied.
     */
    var limit: Int? = null

    /**
     * The number of buckets to skip before returning results. Useful for pagination.
     */
    var offset: Int? = null

    /**
     * A search query to filter buckets by name. If null, no search filter is applied.
     */
    var search: String? = null

    /**
     * The sort order for the results. Can be [SortOrder.ASC] (ascending) or [SortOrder.DESC] (descending).
     * If null, the default sort order from the API is used.
     */
    var sortOrder: SortOrder? = null

    /**
     * The column to sort the results by. If null, the default sort column from the API is used.
     */
    var sortColumn: SortColumn? = null

    internal fun build() = parameters {
        limit?.let { set("limit", it.toString()) }
        offset?.let { set("offset", it.toString()) }
        search?.let { set("search", it) }
        sortOrder?.let { set("sortOrder", it.name.lowercase()) }
        sortColumn?.let { set("sortColumn", it.name.lowercase()) }
    }

    /**
     * Represents the available columns for sorting bucket results.
     */
    enum class SortColumn {
        /** Sort by bucket ID */
        ID,

        /** Sort by bucket name */
        NAME,

        /** Sort by bucket creation timestamp */
        CREATED_AT,

        /** Sort by bucket last updated timestamp */
        UPDATED_AT
    }

}
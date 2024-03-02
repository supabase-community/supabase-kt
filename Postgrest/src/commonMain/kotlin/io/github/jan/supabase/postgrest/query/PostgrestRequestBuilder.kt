@file:Suppress("UndocumentedPublicProperty", "ConstructorParameterNaming")
package io.github.jan.supabase.postgrest.query

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.gotrue.PostgrestFilterDSL
import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import kotlin.js.JsName

/**
 * A builder for Postgrest requests.
 */
@PostgrestFilterDSL
class PostgrestRequestBuilder(@PublishedApi internal val propertyConversionMethod: PropertyConversionMethod) {

    /**
     * The [Count] algorithm to use to count rows in the table or view.
     */
    var count: Count? = null
        private set

    /**
     * The [Returning] option to use.
     */
    var returning: Returning = Returning.Minimal
        private set
    @PublishedApi internal val _params: MutableMap<String, List<String>> = mutableMapOf()
    @PublishedApi internal val headers: HeadersBuilder = HeadersBuilder()
    val params: Map<String, List<String>>
        get() = _params.toMap()

    /**
     * Setting [count] allows to use [PostgrestResult.countOrNull] to get the total amount of items in the database.
     * @param count algorithm to use to count rows in the table or view.
     */
    fun count(count: Count) {
        this.count = count
    }

    /**
     * Return `data` after the query has been executed.
     * @param columns The columns to return
     */
    fun select(columns: Columns = Columns.ALL) {
        this.returning = Returning.Representation(columns)
    }

    /**
     * Return `data` as a single object instead of an array of objects.
     *
     * Query result must be one row (e.g. using `limit(1)`), otherwise this
     * returns an error.
     */
    @JsName("singleValue")
    fun single() {
        headers[HttpHeaders.Accept] = "application/vnd.pgrst.object+json"
    }

    /**
     * Orders the result by [column] in the specified [order].
     * @param column The column to order by
     * @param order The order to use
     * @param nullsFirst If true, null values will be ordered first
     * @param referencedTable If the column is from a foreign table, specify the table name here
     */
    fun order(column: String, order: Order, nullsFirst: Boolean = false, referencedTable: String? = null) {
        val key = if (referencedTable == null) "order" else "$referencedTable.order"
        val existingOrder = _params[key]?.firstOrNull() ?: ""
        val newOrder = "$existingOrder,${column}.${order.value}.${if (nullsFirst) "nullsfirst" else "nullslast"}"
        _params[key] = listOf("${column}.${order.value}.${if (nullsFirst) "nullsfirst" else "nullslast"}")
    }

    /**
     * Limits the result to [count] rows
     * @param count The amount of rows to return
     * @param referencedTable If the column is from a foreign table, specify the table name here
     */
    fun limit(count: Long, referencedTable: String? = null) {
        val key = if (referencedTable == null) "limit" else "$referencedTable.limit"
        _params[key] = listOf(count.toString())
    }

    /**
     * Limits the result to rows from [from] to [to]
     * @param from The first row to return
     * @param to The last row to return
     * @param referencedTable If the column is from a foreign table, specify the table name here
     */
    fun range(from: Long, to: Long, referencedTable: String? = null) {
        val keyOffset = if (referencedTable == null) "offset" else "$referencedTable.offset"
        val keyLimit = if (referencedTable == null) "limit" else "$referencedTable.limit"

        _params[keyOffset] = listOf(from.toString())
        _params[keyLimit] = listOf((to - from + 1).toString())
    }

    /**
     * Limits the result to rows from [range.first] to [range.last]
     * @param range The range of rows to return
     * @param referencedTable If the column is from a foreign table, specify the table name here
     */
    fun range(range: LongRange, referencedTable: String? = null) = range(range.first, range.last, referencedTable)

    /**
     * Return `data` as an object in [GeoJSON](https://geojson.org) format.
     */
    fun geojson() {
        headers[HttpHeaders.Accept] = "application/geo+json"
    }

    /**
     * Return `data` as a string in CSV format.
     */
    fun csv() {
        headers[HttpHeaders.Accept] = "text/csv"
    }

    /**
     * Return `data` as the EXPLAIN plan for the query.
     *
     * @param analyze - If `true`, the query will be executed and the
     * actual run time will be returned
     * @param verbose - If `true`, the query identifier will be returned
     * and `data` will include the output columns of the query
     * @param .settings - If `true`, include information on configuration
     * parameters that affect query planning
     * @param buffers - If `true`, include information on buffer usage
     * @param wal - If `true`, include information on WAL record generation
     * @param format - The format of the output, can be `"text"` (default)
     * or `"json"`
     */
    fun explain(
        analyze: Boolean = false,
        verbose: Boolean = false,
        settings: Boolean = false,
        buffers: Boolean = false,
        wal: Boolean = false,
        format: String = "text",
    ) {
        val options = buildList {
            if (analyze) add("analyze")
            if (verbose) add("verbose")
            if (settings) add("settings")
            if (buffers) add("buffers")
            if (wal) add("wal")
        }.joinToString("|")
        val forMediatype = headers["Accept"] ?: "application/json"
        headers[HttpHeaders.Accept] = "application/vnd.pgrst.plan+${format}; for=\"${forMediatype}\"; options=${options};"
    }

    /**
     * Adds a filter to the postgrest request.
     * @param block The filter block
     */
    inline fun filter(block: @PostgrestFilterDSL PostgrestFilterBuilder.() -> Unit) {
        val filter = PostgrestFilterBuilder(propertyConversionMethod, _params)
        filter.block()
    }

}

@SupabaseInternal
inline fun postgrestRequest(propertyConversionMethod: PropertyConversionMethod = PropertyConversionMethod.CAMEL_CASE_TO_SNAKE_CASE, block: PostgrestRequestBuilder.() -> Unit): PostgrestRequestBuilder {
    val filter = PostgrestRequestBuilder(propertyConversionMethod)
    filter.block()
    return filter
}



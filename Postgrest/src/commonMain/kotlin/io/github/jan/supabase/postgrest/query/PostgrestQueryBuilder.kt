@file:Suppress("UndocumentedPublicProperty")
package io.github.jan.supabase.postgrest.query

import io.github.jan.supabase.encodeToJsonElement
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.PostgrestFilterDSL
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.executor.RestRequestExecutor
import io.github.jan.supabase.postgrest.request.DeleteRequest
import io.github.jan.supabase.postgrest.request.InsertRequest
import io.github.jan.supabase.postgrest.request.SelectRequest
import io.github.jan.supabase.postgrest.request.UpdateRequest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.serialization.json.jsonArray

/**
 * The main class to build a postgrest request
 */
class PostgrestQueryBuilder(
    val postgrest: Postgrest,
    val table: String,
    val schema: String = postgrest.config.defaultSchema,
) {

    /**
     * Executes vertical filtering with select on [table]
     *
     * @param columns The columns to retrieve, defaults to [Columns.ALL]. You can also use [Columns.list], [Columns.type] or [Columns.raw] to specify the columns
     * @param head If true, select will delete the selected data.
     * @param filter Additional filtering to apply to the query
     * @return PostgrestResult which is either an error, an empty JsonArray or the data you requested as an JsonArray
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun select(
        columns: Columns = Columns.ALL,
        head: Boolean = false,
        filter: @PostgrestFilterDSL PostgrestRequestBuilder.() -> Unit = {}
    ): PostgrestResult {
        val requestBuilder = postgrestRequest(postgrest.config.propertyConversionMethod) {
            filter(); _params["select"] = listOf(columns.value)
        }
        val selectRequest = SelectRequest(
            head = head,
            count = requestBuilder.count,
            filter = requestBuilder.params,
            schema = schema,
            headers = requestBuilder.headers.build()
        )
        return RestRequestExecutor.execute(postgrest = postgrest,path = table, request = selectRequest)
    }
    /**
     * Executes an insert operation on the [table]
     *
     * @param values The values to insert, will automatically get serialized into json.
     * @param upsert Performs an upsert if true.
     * @param onConflict When specifying onConflict, you can make upsert work on a columns that has a unique constraint.
     * @param filter Additional filtering to apply to the query
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun <reified T : Any> insert(
        values: List<T>,
        upsert: Boolean = false,
        onConflict: String? = null,
        filter: PostgrestRequestBuilder.() -> Unit = {}
    ): PostgrestResult {
        val requestBuilder = postgrestRequest(postgrest.config.propertyConversionMethod) {
            filter()
            if (upsert && onConflict != null) _params["on_conflict"] = listOf(onConflict)
        }
        val insertRequest = InsertRequest(
            body = postgrest.serializer.encodeToJsonElement(values).jsonArray,
            upsert = upsert,
            onConflict = onConflict,
            returning = requestBuilder.returning,
            count = requestBuilder.count,
            filter = requestBuilder.params,
            schema = schema,
            headers = requestBuilder.headers.build()
        )
        return RestRequestExecutor.execute(postgrest = postgrest, path = table, request = insertRequest)
    }

    /**
     * Executes an insert operation on the [table]
     *
     * @param value The value to insert, will automatically get serialized into json.
     * @param upsert Performs an upsert if true.
     * @param onConflict When specifying onConflict, you can make upsert work on a columns that has a unique constraint.
     * @param filter Additional filtering to apply to the query
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun <reified T : Any> insert(
        value: T,
        upsert: Boolean = false,
        onConflict: String? = null,
        filter: PostgrestRequestBuilder.() -> Unit = {}
    ) = insert(listOf(value), upsert, onConflict, filter)

    /**
     * Executes an update operation on the [table].
     *
     * @param update Specifies the fields to update via a DSL
     * @param filter Additional filtering to apply to the query
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun update(
        crossinline update: PostgrestUpdate.() -> Unit = {},
        filter: PostgrestRequestBuilder.() -> Unit = {}
    ): PostgrestResult {
        val requestBuilder = postgrestRequest(postgrest.config.propertyConversionMethod, filter)
        val updateRequest = UpdateRequest(
            body = buildPostgrestUpdate(postgrest.config.propertyConversionMethod, postgrest.serializer, update),
            returning = requestBuilder.returning,
            count = requestBuilder.count,
            filter = requestBuilder.params,
            schema = schema,
            headers = requestBuilder.headers.build()
        )
        return RestRequestExecutor.execute(postgrest = postgrest,path = table, request = updateRequest)
    }

    /**
     * Executes an update operation on the [table].
     *
     * @param value The value to update, will automatically get serialized into json.
     * @param filter Additional filtering to apply to the query
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun <reified T : Any> update(
        value: T,
        filter: PostgrestRequestBuilder.() -> Unit = {}
    ): PostgrestResult {
        val requestBuilder = postgrestRequest(postgrest.config.propertyConversionMethod, filter)
        val updateRequest = UpdateRequest(
            returning = requestBuilder.returning,
            count = requestBuilder.count,
            filter = requestBuilder.params,
            body = postgrest.serializer.encodeToJsonElement(value),
            schema = schema,
            headers = requestBuilder.headers.build()
        )
        return RestRequestExecutor.execute(postgrest = postgrest, path = table, request = updateRequest)
    }

    /**
     * Executes a delete operation on the [table].
     *
     * @param filter Additional filtering to apply to the query
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun delete(
        filter: PostgrestRequestBuilder.() -> Unit = {}
    ): PostgrestResult {
        val requestBuilder = postgrestRequest(postgrest.config.propertyConversionMethod, filter)
        val deleteRequest = DeleteRequest(
            returning = requestBuilder.returning,
            count = requestBuilder.count,
            filter = requestBuilder.params,
            schema = schema,
            headers = requestBuilder.headers.build()
        )
        return RestRequestExecutor.execute(postgrest = postgrest, path = table, request = deleteRequest)
    }

    companion object {
        const val HEADER_PREFER = "Prefer"
    }

}

/**
 * Used to obtain an estimated amount of rows in a table. See [Postgrest](https://postgrest.org/en/stable/api.html#exact-count) for information about the different count algorithms
 */
enum class Count(val identifier: String) {
    EXACT("exact"),
    PLANNED("planned"),
    ESTIMATED("estimated")
}

/**
 * Can be used to specify whether you want e.g. the inserted row to be returned on creation with all its new fields
 */
enum class Returning(val identifier: String) {
    MINIMAL("minimal"),
    REPRESENTATION("representation"),
    HEADERS_ONLY("headers-only")
}
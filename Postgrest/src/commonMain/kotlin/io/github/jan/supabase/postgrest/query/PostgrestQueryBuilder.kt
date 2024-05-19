@file:Suppress("UndocumentedPublicProperty")
package io.github.jan.supabase.postgrest.query

import io.github.jan.supabase.encodeToJsonElement
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.PostgrestFilterDSL
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.executor.RestRequestExecutor
import io.github.jan.supabase.postgrest.mapToFirstValue
import io.github.jan.supabase.postgrest.request.DeleteRequest
import io.github.jan.supabase.postgrest.request.InsertRequest
import io.github.jan.supabase.postgrest.request.SelectRequest
import io.github.jan.supabase.postgrest.request.UpdateRequest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

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
     * @param head If true, no body will be returned. Useful when using count.
     * @param request Additional filtering to apply to the query
     * @return PostgrestResult which is either an error, an empty JsonArray or the data you requested as an JsonArray
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun select(
        columns: Columns = Columns.ALL,
        head: Boolean = false,
        request: @PostgrestFilterDSL PostgrestRequestBuilder.() -> Unit = {}
    ): PostgrestResult {
        val requestBuilder = postgrestRequest(postgrest.config.propertyConversionMethod) {
            request(); _params["select"] = listOf(columns.value)
        }
        val selectRequest = SelectRequest(
            head = head,
            count = requestBuilder.count,
            urlParams = requestBuilder.params.mapToFirstValue(),
            schema = schema,
            headers = requestBuilder.headers.build()
        )
        return RestRequestExecutor.execute(postgrest = postgrest,path = table, request = selectRequest)
    }

    /**
     * Perform an UPSERT on the table or view. Depending on the column(s) passed
     * to [onConflict], [upsert] allows you to perform the equivalent of
     * `[insert] if a row with the corresponding onConflict columns doesn't
     * exist, or if it does exist, perform an alternative action depending on
     * [ignoreDuplicates].
     *
     * By default, upserted rows are not returned. To return it, call `[PostgrestRequestBuilder.select]`.
     *
     * @param values The values to insert, will automatically get serialized into json.
     * @param request Additional filtering to apply to the query
     * @param onConflict Comma-separated UNIQUE column(s) to specify how
     *  duplicate rows are determined. Two rows are duplicates if all the
     * `onConflict` columns are equal.
     * @param defaultToNull Make missing fields default to `null`.
     * Otherwise, use the default value for the column. This only applies when
     * inserting new rows, not when merging with existing rows under
     * @param ignoreDuplicates If `true`, duplicate rows are ignored. If `false`, duplicate rows are merged with existing rows.
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun <reified T : Any> upsert(
        values: List<T>,
        onConflict: String? = null,
        defaultToNull: Boolean = true,
        ignoreDuplicates: Boolean = false,
        request: PostgrestRequestBuilder.() -> Unit = {}
    ): PostgrestResult {
        val requestBuilder = postgrestRequest(postgrest.config.propertyConversionMethod, request)
        val body = postgrest.serializer.encodeToJsonElement(values).jsonArray
        val columns = body.map { it.jsonObject.keys }.flatten().distinct()
        requestBuilder._params["columns"] = listOf(columns.joinToString(","))
        onConflict?.let {
            requestBuilder._params["on_conflict"] = listOf(it)
        }
        val insertRequest = InsertRequest(
            body = body,
            upsert = true,
            returning = requestBuilder.returning,
            count = requestBuilder.count,
            urlParams = requestBuilder.params.mapToFirstValue(),
            defaultToNull = defaultToNull,
            ignoreDuplicates = ignoreDuplicates,
            schema = schema,
            headers = requestBuilder.headers.build()
        )
        return RestRequestExecutor.execute(postgrest = postgrest, path = table, request = insertRequest)
    }

    /**
     * Perform an UPSERT on the table or view. Depending on the column(s) passed
     * to [onConflict], [upsert] allows you to perform the equivalent of
     * `[insert] if a row with the corresponding onConflict columns doesn't
     * exist, or if it does exist, perform an alternative action depending on
     * [ignoreDuplicates].
     *
     * By default, upserted rows are not returned. To return it, call `[PostgrestRequestBuilder.select]`.
     *
     * @param value The value to insert, will automatically get serialized into json.
     * @param request Additional filtering to apply to the query
     * @param onConflict Comma-separated UNIQUE column(s) to specify how
     *  duplicate rows are determined. Two rows are duplicates if all the
     * `onConflict` columns are equal.
     * @param defaultToNull Make missing fields default to `null`.
     * Otherwise, use the default value for the column. This only applies when
     * inserting new rows, not when merging with existing rows under
     * @param ignoreDuplicates If `true`, duplicate rows are ignored. If `false`, duplicate rows are merged with existing rows.
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun <reified T : Any> upsert(
        value: T,
        onConflict: String? = null,
        defaultToNull: Boolean = true,
        ignoreDuplicates: Boolean = false,
        request: PostgrestRequestBuilder.() -> Unit = {}
    ): PostgrestResult = upsert(listOf(value), onConflict, defaultToNull, ignoreDuplicates, request)

    /**
     * Executes an insert operation on the [table]
     *
     * @param values The values to insert, will automatically get serialized into json.
     * @param request Additional filtering to apply to the query
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun <reified T : Any> insert(
        values: List<T>,
        request: PostgrestRequestBuilder.() -> Unit = {}
    ): PostgrestResult {
        val requestBuilder = postgrestRequest(postgrest.config.propertyConversionMethod, request)
        val insertRequest = InsertRequest(
            body = postgrest.serializer.encodeToJsonElement(values).jsonArray,
            returning = requestBuilder.returning,
            count = requestBuilder.count,
            urlParams = requestBuilder.params.mapToFirstValue(),
            schema = schema,
            headers = requestBuilder.headers.build()
        )
        return RestRequestExecutor.execute(postgrest = postgrest, path = table, request = insertRequest)
    }

    /**
     * Executes an insert operation on the [table]
     *
     * @param value The value to insert, will automatically get serialized into json.
     * @param request Additional filtering to apply to the query
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun <reified T : Any> insert(
        value: T,
        request: PostgrestRequestBuilder.() -> Unit = {}
    ) = insert(listOf(value), request)

    /**
     * Executes an update operation on the [table].
     *
     * By default, updated rows are not returned. To return it, call `[PostgrestRequestBuilder.select]`.
     *
     * @param update Specifies the fields to update via a DSL
     * @param request Additional filtering to apply to the query
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun update(
        crossinline update: PostgrestUpdate.() -> Unit = {},
        request: PostgrestRequestBuilder.() -> Unit = {}
    ): PostgrestResult {
        val requestBuilder = postgrestRequest(postgrest.config.propertyConversionMethod, request)
        val updateRequest = UpdateRequest(
            body = buildPostgrestUpdate(postgrest.config.propertyConversionMethod, postgrest.serializer, update),
            returning = requestBuilder.returning,
            count = requestBuilder.count,
            urlParams = requestBuilder.params.mapToFirstValue(),
            schema = schema,
            headers = requestBuilder.headers.build()
        )
        return RestRequestExecutor.execute(postgrest = postgrest,path = table, request = updateRequest)
    }

    /**
     * Executes an update operation on the [table].
     *
     * By default, updated rows are not returned. To return it, call `[PostgrestRequestBuilder.select]`.
     *
     * @param value The value to update, will automatically get serialized into json.
     * @param request Additional filtering to apply to the query
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun <reified T : Any> update(
        value: T,
        request: PostgrestRequestBuilder.() -> Unit = {}
    ): PostgrestResult {
        val requestBuilder = postgrestRequest(postgrest.config.propertyConversionMethod, request)
        val updateRequest = UpdateRequest(
            returning = requestBuilder.returning,
            count = requestBuilder.count,
            urlParams = requestBuilder.params.mapToFirstValue(),
            body = postgrest.serializer.encodeToJsonElement(value),
            schema = schema,
            headers = requestBuilder.headers.build()
        )
        return RestRequestExecutor.execute(postgrest = postgrest, path = table, request = updateRequest)
    }

    /**
     * Executes a delete operation on the [table].
     *
     * By default, deleted rows are not returned. To return it, call `[PostgrestRequestBuilder.select]`.
     *
     * @param request Additional filtering to apply to the query
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun delete(
        request: PostgrestRequestBuilder.() -> Unit = {}
    ): PostgrestResult {
        val requestBuilder = postgrestRequest(postgrest.config.propertyConversionMethod, request)
        val deleteRequest = DeleteRequest(
            returning = requestBuilder.returning,
            count = requestBuilder.count,
            urlParams = requestBuilder.params.mapToFirstValue(),
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
sealed class Returning(val identifier: String) {

    /**
     * Doesn't return any data
     */
    data object Minimal: Returning("minimal")

    /**
     * Returns data based on the [columns] specified
     * @param columns The columns to return, defaults to [Columns.ALL]. You can also use [Columns.list], [Columns.type] or [Columns.raw] to specify the columns
     */
    data class Representation(val columns: Columns = Columns.ALL): Returning("representation")
}
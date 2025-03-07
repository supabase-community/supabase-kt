@file:Suppress("UndocumentedPublicProperty")
package io.github.jan.supabase.postgrest.query

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.AuthUserScope
import io.github.jan.supabase.auth.PostgrestFilterDSL
import io.github.jan.supabase.encodeToJsonElement
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.executor.RestRequestExecutor
import io.github.jan.supabase.postgrest.mapToFirstValue
import io.github.jan.supabase.postgrest.query.request.InsertRequestBuilder
import io.github.jan.supabase.postgrest.query.request.SelectRequestBuilder
import io.github.jan.supabase.postgrest.query.request.UpsertRequestBuilder
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
): AuthUserScope {

    override val supabase: SupabaseClient = postgrest.supabaseClient

    /**
     * Executes vertical filtering with select on [table]
     *
     * @param columns The columns to retrieve, defaults to [Columns.ALL]. You can also use [Columns.list], [Columns.type] or [Columns.raw] to specify the columns
     * @param request Additional configurations for the request including filters
     * @return PostgrestResult which is either an error, an empty JsonArray or the data you requested as an JsonArray
     * @throws PostgrestRestException if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun select(
        columns: Columns = Columns.ALL,
        request: @PostgrestFilterDSL SelectRequestBuilder.() -> Unit = {}
    ): PostgrestResult {
        val requestBuilder = SelectRequestBuilder(postgrest.config.propertyConversionMethod).apply {
            request(); params["select"] = listOf(columns.value)
        }
        val selectRequest = SelectRequest(
            head = requestBuilder.head,
            count = requestBuilder.count,
            urlParams = requestBuilder.params.mapToFirstValue(),
            schema = schema,
            headers = requestBuilder.headers.build()
        )
        return RestRequestExecutor.execute(postgrest = postgrest,path = table, request = selectRequest)
    }

    /**
     * Perform an UPSERT on the table or view. Depending on the column(s) passed
     * to [UpsertRequestBuilder.onConflict], [upsert] allows you to perform the equivalent of
     * `[insert] if a row with the corresponding onConflict columns doesn't
     * exist, or if it does exist, perform an alternative action depending on
     * [UpsertRequestBuilder.ignoreDuplicates].
     *
     * By default, upserted rows are not returned. To return it, call `[PostgrestRequestBuilder.select]`.
     *
     * @param values The values to insert, will automatically get serialized into json.
     * @param request Additional configurations for the request including filters
     * @throws PostgrestRestException if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun <reified T : Any> upsert(
        values: List<T>,
        request: UpsertRequestBuilder.() -> Unit = {}
    ): PostgrestResult {
        val requestBuilder = UpsertRequestBuilder(postgrest.config.propertyConversionMethod).apply(request)
        val body = postgrest.serializer.encodeToJsonElement(values).jsonArray
        val columns = body.map { it.jsonObject.keys }.flatten().distinct()
        if(columns.isNotEmpty()) requestBuilder.params["columns"] = listOf(columns.joinToString(","))
        requestBuilder.onConflict?.let {
            requestBuilder.params["on_conflict"] = listOf(it)
        }
        val insertRequest = InsertRequest(
            body = body,
            upsert = true,
            returning = requestBuilder.returning,
            count = requestBuilder.count,
            urlParams = requestBuilder.params.mapToFirstValue(),
            defaultToNull = requestBuilder.defaultToNull,
            ignoreDuplicates = requestBuilder.ignoreDuplicates,
            schema = schema,
            headers = requestBuilder.headers.build()
        )
        return RestRequestExecutor.execute(postgrest = postgrest, path = table, request = insertRequest)
    }

    /**
     * Perform an UPSERT on the table or view. Depending on the column(s) passed
     * to [UpsertRequestBuilder.onConflict], [upsert] allows you to perform the equivalent of
     * `[insert] if a row with the corresponding onConflict columns doesn't
     * exist, or if it does exist, perform an alternative action depending on
     * [UpsertRequestBuilder.ignoreDuplicates].
     *
     * By default, upserted rows are not returned. To return it, call `[PostgrestRequestBuilder.select]`.
     *
     * @param value The value to insert, will automatically get serialized into json.
     * @param request Additional filtering to apply to the query
     * @throws PostgrestRestException if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun <reified T : Any> upsert(
        value: T,
        request: UpsertRequestBuilder.() -> Unit = {}
    ): PostgrestResult = upsert(listOf(value), request)

    /**
     * Executes an insert operation on the [table]
     *
     * @param values The values to insert, will automatically get serialized into json.
     * @param request Additional filtering to apply to the query
     * @throws PostgrestRestException if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun <reified T : Any> insert(
        values: List<T>,
        request: InsertRequestBuilder.() -> Unit = {}
    ): PostgrestResult {
        val requestBuilder = InsertRequestBuilder(postgrest.config.propertyConversionMethod).apply(request)
        val body = postgrest.serializer.encodeToJsonElement(values).jsonArray
        val columns = body.map { it.jsonObject.keys }.flatten().distinct()
        if(columns.isNotEmpty()) requestBuilder.params["columns"] = listOf(columns.joinToString(","))
        val insertRequest = InsertRequest(
            body = body,
            returning = requestBuilder.returning,
            count = requestBuilder.count,
            urlParams = requestBuilder.params.mapToFirstValue(),
            schema = schema,
            headers = requestBuilder.headers.build(),
            defaultToNull = requestBuilder.defaultToNull
        )
        return RestRequestExecutor.execute(postgrest = postgrest, path = table, request = insertRequest)
    }

    /**
     * Executes an insert operation on the [table]
     *
     * @param value The value to insert, will automatically get serialized into json.
     * @param request Additional filtering to apply to the query
     * @throws PostgrestRestException if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun <reified T : Any> insert(
        value: T,
        request: InsertRequestBuilder.() -> Unit = {}
    ) = insert(listOf(value), request)

    /**
     * Executes an update operation on the [table].
     *
     * By default, updated rows are not returned. To return it, call `[PostgrestRequestBuilder.select]`.
     *
     * @param update Specifies the fields to update via a DSL
     * @param request Additional filtering to apply to the query
     * @throws PostgrestRestException if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun update(
        crossinline update: PostgrestUpdate.() -> Unit = {},
        request: PostgrestRequestBuilder.() -> Unit = {}
    ): PostgrestResult {
        val requestBuilder = PostgrestRequestBuilder(postgrest).apply(request)
        val updateRequest = UpdateRequest(
            body = buildPostgrestUpdate(postgrest, postgrest.serializer, update),
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
     * @throws PostgrestRestException if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun <reified T : Any> update(
        value: T,
        request: PostgrestRequestBuilder.() -> Unit = {}
    ): PostgrestResult {
        val requestBuilder = PostgrestRequestBuilder(postgrest).apply(request)
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
     * @throws PostgrestRestException if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun delete(
        request: PostgrestRequestBuilder.() -> Unit = {}
    ): PostgrestResult {
        val requestBuilder = PostgrestRequestBuilder(postgrest).apply(request)
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
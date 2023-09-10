@file:Suppress("UndocumentedPublicProperty")
package io.github.jan.supabase.postgrest.query

import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.encodeToJsonElement
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.PostgrestFilterDSL
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.executor.RequestExecutor
import io.github.jan.supabase.postgrest.request.DeleteRequest
import io.github.jan.supabase.postgrest.request.InsertRequest
import io.github.jan.supabase.postgrest.request.RpcRequest
import io.github.jan.supabase.postgrest.request.SelectRequest
import io.github.jan.supabase.postgrest.request.UpdateRequest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray

/**
 * The main class to build a postgrest request
 */
class PostgrestBuilder(
    val postgrest: Postgrest,
    val table: String,
    val schema: String = postgrest.config.defaultSchema,
    val requestExecutor: RequestExecutor,
    val config: Postgrest.Config
) {

    /**
     * Executes vertical filtering with select on [table]
     *
     * @param columns The columns to retrieve, defaults to [Columns.ALL]. You can also use [Columns.list], [Columns.type] or [Columns.raw] to specify the columns
     * @param head If true, select will delete the selected data.
     * @param count Count algorithm to use to count rows in a table.
     * @param single If true, select will return a single row. Throws an error if the query returns more than one row.
     * @param filter Additional filtering to apply to the query
     * @return PostgrestResult which is either an error, an empty JsonArray or the data you requested as an JsonArray
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun select(
        columns: Columns = Columns.ALL,
        head: Boolean = false,
        count: Count? = null,
        single: Boolean = false,
        filter: @PostgrestFilterDSL PostgrestFilterBuilder.() -> Unit = {}
    ): PostgrestResult {
        val selectRequest = SelectRequest(
            head = head,
            count = count,
            single = single,
            filter = buildPostgrestFilter(postgrest.config.propertyConversionMethod) {
                filter(); _params["select"] = listOf(columns.value)
            },
            schema = schema
        )
        return requestExecutor.execute(path = table, request = selectRequest)
    }
    /**
     * Executes an insert operation on the [table]
     *
     * @param values The values to insert, will automatically get serialized into json.
     * @param upsert Performs an upsert if true.
     * @param onConflict When specifying onConflict, you can make upsert work on a columns that has a unique constraint.
     * @param returning By default, the new record is returned. You can set this to 'minimal' if you don't need this value
     * @param count Count algorithm to use to count rows in a table
     * @param filter Additional filtering to apply to the query
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun <reified T : Any> insert(
        values: List<T>,
        upsert: Boolean = false,
        onConflict: String? = null,
        returning: Returning = Returning.REPRESENTATION,
        count: Count? = null,
        filter: PostgrestFilterBuilder.() -> Unit = {}
    ): PostgrestResult {
        val insertRequest = InsertRequest(
            body = postgrest.serializer.encodeToJsonElement(values).jsonArray,
            upsert = upsert,
            onConflict = onConflict,
            returning = returning,
            count = count,
            filter = buildPostgrestFilter(postgrest.config.propertyConversionMethod) {
                filter()
                if (upsert && onConflict != null) _params["on_conflict"] = listOf(onConflict)
            },
            schema = schema
        )
        return requestExecutor.execute(table, insertRequest)
    }

    /**
     * Executes an insert operation on the [table]
     *
     * @param value The value to insert, will automatically get serialized into json.
     * @param upsert Performs an upsert if true.
     * @param onConflict When specifying onConflict, you can make upsert work on a columns that has a unique constraint.
     * @param returning By default, the new record is returned. You can set this to 'minimal' if you don't need this value
     * @param count Count algorithm to use to count rows in a table
     * @param filter Additional filtering to apply to the query
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun <reified T : Any> insert(
        value: T,
        upsert: Boolean = false,
        onConflict: String? = null,
        returning: Returning = Returning.REPRESENTATION,
        count: Count? = null,
        filter: PostgrestFilterBuilder.() -> Unit = {}
    ) = insert(listOf(value), upsert, onConflict, returning, count, filter)

    /**
     * Executes an update operation on the [table].
     *
     * @param update Specifies the fields to update via a DSL
     * @param count Count algorithm to use to count rows in a table
     * @param filter Additional filtering to apply to the query
     * @param returning By default, the new record is returned. You can set this to 'minimal' if you don't need this value
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun update(
        crossinline update: PostgrestUpdate.() -> Unit = {},
        returning: Returning = Returning.REPRESENTATION,
        count: Count? = null,
        filter: PostgrestFilterBuilder.() -> Unit = {}
    ): PostgrestResult = update(
        value = buildPostgrestUpdate(
            propertyConversionMethod = postgrest.config.propertyConversionMethod,
            block = update
        ), returning = returning, count = count, filter = filter
    )

    /**
     * Executes an update operation on the [table].
     *
     * @param value The value to update, will automatically get serialized into json.
     * @param count Count algorithm to use to count rows in a table
     * @param filter Additional filtering to apply to the query
     * @param returning By default, the new record is returned. You can set this to 'minimal' if you don't need this value
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun <reified T : Any> update(
        value: T,
        returning: Returning = Returning.REPRESENTATION,
        count: Count? = null,
        filter: PostgrestFilterBuilder.() -> Unit = {}
    ): PostgrestResult {
        val updateRequest = UpdateRequest(
            returning = returning,
            count = count,
            filter = buildPostgrestFilter(postgrest.config.propertyConversionMethod, filter),
            body = postgrest.serializer.encodeToJsonElement(value),
            schema = schema
        )
        return requestExecutor.execute(path = table, request = updateRequest)
    }

    /**
     * Executes a delete operation on the [table].
     *
     * @param returning If set to true, you get the deleted rows as the response
     * @param count Count algorithm to use to count rows in a table
     * @param filter Additional filtering to apply to the query
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline fun delete(
        returning: Returning = Returning.REPRESENTATION,
        count: Count? = null,
        filter: PostgrestFilterBuilder.() -> Unit = {}
    ): PostgrestResult {
        val deleteRequest = DeleteRequest(
            returning = returning,
            count = count,
            filter = buildPostgrestFilter(postgrest.config.propertyConversionMethod, filter),
            schema = schema
        )
        return requestExecutor.execute(path = table, request = deleteRequest)
    }


    /**
     * Executes a database function
     *
     * @param function The name of the function
     * @param parameters The parameters for the function
     * @param head If true, select will delete the selected data.
     * @param count Count algorithm to use to count rows in a table.
     * @param filter Filter the result
     * @throws RestException or one of its subclasses if the request failed
     */
    suspend inline fun <reified T : Any> rpc(
        function: String,
        parameters: T,
        head: Boolean = false,
        count: Count? = null,
        filter: PostgrestFilterBuilder.() -> Unit = {},
        serializer: SupabaseSerializer
    ) {
        val rpcRequest =
            RpcRequest(
                head,
                count,
                PostgrestFilterBuilder(config.propertyConversionMethod).apply(filter).params,
                if (parameters is JsonElement) parameters else serializer.encodeToJsonElement(
                    parameters
                )
            )
        requestExecutor.execute(path = "rpc/$function", request = rpcRequest)
    }

    /**
     * Executes a database function
     *
     * @param function The name of the function
     * @param head If true, select will delete the selected data.
     * @param count Count algorithm to use to count rows in a table.
     * @param filter Filter the result
     * @throws RestException or one of its subclasses if the request failed
     */
    suspend fun rpc(
        function: String,
        head: Boolean = false,
        count: Count? = null,
        filter: PostgrestFilterBuilder.() -> Unit = {}
    ) {
        val rpcRequest = RpcRequest(
            head = head,
            count = count,
            filter = PostgrestFilterBuilder(config.propertyConversionMethod).apply(filter).params
        )
        requestExecutor.execute(path = "rpc/$function", request = rpcRequest)
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
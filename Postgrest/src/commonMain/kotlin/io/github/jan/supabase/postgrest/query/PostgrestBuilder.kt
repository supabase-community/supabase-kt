package io.github.jan.supabase.postgrest.query

import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.request.PostgrestRequest
import io.github.jan.supabase.supabaseJson
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlin.experimental.ExperimentalTypeInference

/**
 * The main class to build a postgrest request
 */
class PostgrestBuilder(val postgrest: Postgrest, val table: String) {

    /**
     * Executes vertical filtering with select on [table]
     *
     * @param columns The columns to retrieve, separated by commas.
     * @param head If true, select will delete the selected data.
     * @param count Count algorithm to use to count rows in a table.
     * @param filter Additional filtering to apply to the query
     * @return PostgrestResult which is either an error, an empty JsonArray or the data you requested as an JsonArray
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    @OptIn(ExperimentalTypeInference::class)
    suspend inline fun select(
        columns: String = "*",
        head: Boolean = false,
        count: Count? = null,
        @BuilderInference filter: PostgrestFilterBuilder.() -> Unit = {}
    ): PostgrestResult = PostgrestRequest.Select(head, count, buildPostgrestFilter { filter(); _params["select"] = columns }).execute(table, postgrest)

    /**
     * Executes an insert operation on the [table]
     *
     * @param values The values to insert, will automatically get serialized into json.
     * @param upsert Performs an upsert if true.
     * @param onConflict When specifying onConflict, you can make upsert work on a columns that has a unique constraint.
     * @param returning By default, the new record is returned. You can set this to 'minimal' if you don't need this value
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
    ): PostgrestResult = PostgrestRequest.Insert(supabaseJson.encodeToJsonElement(values).jsonArray, upsert, onConflict, returning, count, buildPostgrestFilter {
        filter()
        if (upsert && onConflict != null) _params["on_conflict"] = onConflict
    }).execute(table, postgrest)

    /**
     * Executes an insert operation on the [table]
     *
     * @param value The value to insert, will automatically get serialized into json.
     * @param upsert Performs an upsert if true.
     * @param onConflict When specifying onConflict, you can make upsert work on a columns that has a unique constraint.
     * @param returning By default, the new record is returned. You can set this to 'minimal' if you don't need this value
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
     * @param returning By default, the new record is returned. You can set this to 'minimal' if you don't need this value
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    @OptIn(ExperimentalTypeInference::class)
    suspend inline fun update(
        crossinline update: PostgrestUpdate.() -> Unit = {},
        returning: Returning = Returning.REPRESENTATION,
        count: Count? = null,
        @BuilderInference filter: PostgrestFilterBuilder.() -> Unit = {}
    ): PostgrestResult = PostgrestRequest.Update(returning, count, buildPostgrestFilter(filter), buildPostgrestUpdate(update)).execute(table, postgrest)

    /**
     * Executes a delete operation on the [table].
     *
     * @param returning If set to true, you get the deleted rows as the response
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    @OptIn(ExperimentalTypeInference::class)
    suspend inline fun delete(
        returning: Returning = Returning.REPRESENTATION,
        count: Count? = null,
        @BuilderInference filter: PostgrestFilterBuilder.() -> Unit = {}
    ): PostgrestResult = PostgrestRequest.Delete(returning, count, buildPostgrestFilter(filter)).execute(table, postgrest)

    companion object {
        const val HEADER_PREFER = "Prefer"
    }

}

enum class Count(val identifier: String) {
    EXACT("exact"),
    PLANNED("planned"),
    ESTIMATED("estimated")
}

enum class Returning(val identifier: String) {
    MINIMAL("minimal"),
    REPRESENTATION("representation")
}
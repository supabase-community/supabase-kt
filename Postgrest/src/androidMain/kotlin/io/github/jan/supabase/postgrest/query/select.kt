package io.github.jan.supabase.postgrest.query

import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.request.PostgrestRequest
import io.ktor.client.plugins.*
import kotlin.reflect.full.primaryConstructor

/**
 * Executes vertical filtering with select on [PostgrestBuilder.table]
 *
 * T is the desired data class as a result of the selection.
 * @param head If true, select will delete the selected data.
 * @param count Count algorithm to use to count rows in a table.
 * @param single If true, select will return a single row. Throws an error if the query returns more than one row.
 * @param filter Additional filtering to apply to the query
 * @return PostgrestResult which is either an error, an empty JsonArray or the data you requested as an JsonArray
 * @throws RestException or one of its subclasses if receiving an error response
 * @throws HttpRequestTimeoutException if the request timed out
 * @throws HttpRequestException on network related issues
 */
//TODO add propertyConversionMethod to it.name in parameters.map
suspend inline fun <reified T: Any> PostgrestBuilder.select(
    head: Boolean = false,
    count: Count? = null,
    single: Boolean = false,
    filter: PostgrestFilterBuilder.() -> Unit = {}
): PostgrestResult {
    if (T::class.isData) {
        val columns = T::class.primaryConstructor!!.parameters.map { it.name }.joinToString()
       return PostgrestRequest.Select(head, count, single, buildPostgrestFilter { filter(); _params["select"] = listOf(columns) }, schema).execute(table, postgrest)
    } else throw Throwable("T should be a data class")
}
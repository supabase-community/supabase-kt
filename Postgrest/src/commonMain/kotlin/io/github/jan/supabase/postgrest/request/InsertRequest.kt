package io.github.jan.supabase.postgrest.request

import io.github.jan.supabase.postgrest.query.Count
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonArray

@PublishedApi
internal class InsertRequest(
    private val preferOptions: PreferOptions,
    override val urlParamOptions: PostgrestRequest.UrlParamOptions,
    httpOptions: HttpOptions,
    headerOptions: HeaderOptions
) : PostgrestRequest {

    override val httpOptions: PostgrestRequest.HttpOptions = PostgrestRequest.HttpOptions(
        method = HttpMethod.Post,
        body = httpOptions.body
    )
    override val headerOptions: PostgrestRequest.HeaderOptions = PostgrestRequest.HeaderOptions(
        prefer = buildList {
            add("return=${urlParamOptions.returning.identifier}")
            if (preferOptions.upsert) add("resolution=${if (preferOptions.ignoreDuplicates) "ignore" else "merge"}-duplicates")
            if(!preferOptions.defaultToNull) add("missing=default")
            if (preferOptions.count != null) add("count=${preferOptions.count.identifier}")
        },
        headers = headerOptions.headers,
        schema = headerOptions.schema,
        stripNulls = headerOptions.stripNulls
    )

    data class PreferOptions(
        val upsert: Boolean = false,
        val count: Count? = null,
        val ignoreDuplicates: Boolean = false,
        val defaultToNull: Boolean = false,
    )

    data class HeaderOptions(
        val schema: String,
        val headers: Headers = Headers.Empty,
        val stripNulls: Boolean
    )

    data class HttpOptions(
        val body: JsonArray
    )

}

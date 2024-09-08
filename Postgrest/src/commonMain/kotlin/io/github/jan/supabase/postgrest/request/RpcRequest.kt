package io.github.jan.supabase.postgrest.request

import io.github.jan.supabase.postgrest.query.Count
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonElement

@PublishedApi
internal class RpcRequest(
    override val method: HttpMethod,
    val count: Count? = null,
    override val urlParams: Map<String, String>,
    override val body: JsonElement? = null,
    override val schema: String = "public",
    override val headers: Headers = Headers.Empty
) : PostgrestRequest {

    override val prefer = if (count != null) listOf("count=${count.identifier}") else listOf()

}
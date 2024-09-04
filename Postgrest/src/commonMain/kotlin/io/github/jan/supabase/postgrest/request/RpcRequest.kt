package io.github.jan.supabase.postgrest.request

import io.github.jan.supabase.postgrest.query.Count
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonElement
import io.ktor.http.Headers

@PublishedApi
internal class RpcRequest(
    override val method: HttpMethod,
    val count: Count? = null,
    override val urlParams: Map<String, String>,
    override val body: JsonElement? = null,
    override val headers: Headers = Headers.Empty
) : PostgrestRequest {

    override val schema: String = ""
    override val prefer = if (count != null) listOf("count=${count.identifier}") else listOf()

}

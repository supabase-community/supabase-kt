package io.github.jan.supabase.postgrest.request.impl

import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.request.PostgrestRequest
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonElement

class RPC(
    head: Boolean = false,
    count: Count? = null,
    override val filter: Map<String, List<String>>,
    override val body: JsonElement? = null,
) : PostgrestRequest {

    override val schema: String = ""

    override val method = if (head) HttpMethod.Head else HttpMethod.Post
    override val prefer = if (count != null) listOf("count=${count.identifier}") else listOf()

}
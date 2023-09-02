package io.github.jan.supabase.postgrest.request.impl

import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.request.PostgrestRequest
import io.ktor.http.HttpMethod

class SelectRequest(
    head: Boolean = false,
    count: Count? = null,
    override val single: Boolean = false,
    override val filter: Map<String, List<String>>,
    override val schema: String
): PostgrestRequest {

    override val method = if(head) HttpMethod.Head else HttpMethod.Get
    override val prefer = if (count != null) listOf("count=${count.identifier}") else listOf()

}
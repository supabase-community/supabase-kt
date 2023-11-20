package io.github.jan.supabase.postgrest.request

import io.github.jan.supabase.postgrest.query.Count
import io.ktor.http.Headers
import io.ktor.http.HttpMethod

@PublishedApi
 internal class SelectRequest(
    val head: Boolean = false,
    val count: Count? = null,
    override val filter: Map<String, List<String>>,
    override val schema: String,
    override val headers: Headers
): PostgrestRequest {

    override val method = if (head) HttpMethod.Head else HttpMethod.Get
    override val prefer = if (count != null) listOf("count=${count.identifier}") else listOf()

}
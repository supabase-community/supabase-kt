package io.github.jan.supabase.postgrest.request

import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Returning
import io.ktor.http.Headers
import io.ktor.http.HttpMethod

@PublishedApi
internal class DeleteRequest(
    private val returning: Returning = Returning.MINIMAL,
    private val count: Count? = null,
    override val urlParams: Map<String, String>,
    override val schema: String,
    override val headers: Headers = Headers.Empty,
) : PostgrestRequest {

    override val method = HttpMethod.Delete
    override val prefer = buildList {
        add("return=${returning.identifier}")
        if (count != null) add("count=${count.identifier}")
    }

}
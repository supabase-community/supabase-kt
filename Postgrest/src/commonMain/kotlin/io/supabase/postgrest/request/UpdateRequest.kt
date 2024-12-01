package io.supabase.postgrest.request

import io.supabase.postgrest.query.Count
import io.supabase.postgrest.query.Returning
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonElement

@PublishedApi
internal class UpdateRequest(
    override val returning: Returning = Returning.Minimal,
    private val count: Count? = null,
    override val urlParams: Map<String, String>,
    override val body: JsonElement,
    override val schema: String,
    override val headers: Headers = Headers.Empty,
) : PostgrestRequest {

    override val method = HttpMethod.Patch
    override val prefer = buildList {
        add("return=${returning.identifier}")
        if (count != null) add("count=${count.identifier}")
    }

}
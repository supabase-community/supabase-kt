package io.github.jan.supabase.postgrest.request

import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Returning
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonElement

@PublishedApi
internal class UpdateRequest(
    private val returning: Returning = Returning.REPRESENTATION,
    private val count: Count? = null,
    override val filter: Map<String, List<String>>,
    override val body: JsonElement,
    override val schema: String,
    override val headers: Headers
) : PostgrestRequest {

    override val method = HttpMethod.Patch
    override val prefer = buildList {
        add("return=${returning.identifier}")
        if (count != null) add("count=${count.identifier}")
    }

}
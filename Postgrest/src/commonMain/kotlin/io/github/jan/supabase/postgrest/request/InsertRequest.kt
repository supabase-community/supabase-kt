package io.github.jan.supabase.postgrest.request

import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Returning
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonArray

@PublishedApi
internal data class InsertRequest(
    private val upsert: Boolean = false,
    private val onConflict: String? = null,
    private val returning: Returning = Returning.REPRESENTATION,
    private val count: Count? = null,
    override val body: JsonArray,
    override val filter: Map<String, List<String>>,
    override val schema: String,
    override val headers: Headers
) : PostgrestRequest {

    override val method = HttpMethod.Post
    override val prefer = buildList {
        add("return=${returning.identifier}")
        if (upsert) add("resolution=merge-duplicates")
        if (count != null) add("count=${count.identifier}")
    }
    override val urlParams =
        if (upsert && onConflict != null) mapOf("on_conflict" to onConflict) else mapOf()

}

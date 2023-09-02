package io.github.jan.supabase.postgrest.request.impl

import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Returning
import io.github.jan.supabase.postgrest.request.PostgrestRequest
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonArray


class Insert(
    override val body: JsonArray,
    private val upsert: Boolean = false,
    private val onConflict: String? = null,
    private val returning: Returning = Returning.REPRESENTATION,
    private val count: Count? = null,
    override val filter: Map<String, List<String>>,
    override val schema: String
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

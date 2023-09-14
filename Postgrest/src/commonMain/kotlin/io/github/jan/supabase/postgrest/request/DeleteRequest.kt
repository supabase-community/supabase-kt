package io.github.jan.supabase.postgrest.request

import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Returning
import io.ktor.http.HttpMethod

class DeleteRequest(
    private val returning: Returning = Returning.REPRESENTATION,
    private val count: Count? = null,
    override val filter: Map<String, List<String>>,
    override val schema: String
) : PostgrestRequest {

    override val method = HttpMethod.Delete
    override val prefer = buildList {
        add("return=${returning.identifier}")
        if (count != null) add("count=${count.identifier}")
    }

}
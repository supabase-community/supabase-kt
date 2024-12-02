@file:Suppress(
    "UndocumentedPublicClass",
    "UndocumentedPublicFunction",
    "UndocumentedPublicProperty"
)

package io.supabase.postgrest.request

import io.supabase.annotations.SupabaseInternal
import io.supabase.postgrest.query.Returning
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonElement

@SupabaseInternal
sealed interface PostgrestRequest {

    val body: JsonElement? get() = null
    val method: HttpMethod
    val urlParams: Map<String, String>
    val headers: Headers get() = Headers.Empty
    val returning: Returning get() = Returning.Minimal
    val prefer: List<String>
    val schema: String

}
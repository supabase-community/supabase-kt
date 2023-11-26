@file:Suppress(
    "UndocumentedPublicClass",
    "UndocumentedPublicFunction",
    "UndocumentedPublicProperty"
)

package io.github.jan.supabase.postgrest.request

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.postgrest.query.Returning
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
@file:Suppress(
    "UndocumentedPublicClass",
    "UndocumentedPublicFunction",
    "UndocumentedPublicProperty"
)

package io.github.jan.supabase.postgrest.request

import io.github.jan.supabase.annotations.SupabaseInternal
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonElement

@SupabaseInternal
sealed interface PostgrestRequest {

    val body: JsonElement? get() = null
    val method: HttpMethod
    val filter: Map<String, List<String>>
    val prefer: List<String>
    val single: Boolean get() = false
    val urlParams: Map<String, String> get() = mapOf()
    val schema: String

}
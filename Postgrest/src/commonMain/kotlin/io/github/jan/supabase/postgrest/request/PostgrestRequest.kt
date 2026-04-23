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

    val httpOptions: HttpOptions
    val urlParamOptions: UrlParamOptions
    val headerOptions: HeaderOptions
    val metaOptions: MetaOptions get() = MetaOptions(true)

    data class HttpOptions(
        val body: JsonElement? = null,
        val method: HttpMethod
    )

    data class UrlParamOptions(
        val urlParams: Map<String, String> = emptyMap(),
        val returning: Returning = Returning.Minimal
    )

    data class HeaderOptions(
        val headers: Headers = Headers.Empty,
        val schema: String,
        val prefer: List<String>,
        val stripNulls: Boolean
    )

    data class MetaOptions(
        val retry: Boolean = true
    )

}
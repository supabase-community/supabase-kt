@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "UndocumentedPublicProperty")
package io.github.jan.supabase.postgrest.request

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.bodyOrNull
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonElement

@SupabaseInternal
 interface PostgrestRequest {

    val body: JsonElement? get() = null
    val method: HttpMethod
    val filter: Map<String, List<String>>
    val prefer: List<String>
    val single: Boolean get() = false
    val urlParams: Map<String, String> get() = mapOf()
    val schema: String

    private suspend fun HttpResponse.asPostgrestResult(postgrest: Postgrest): PostgrestResult = PostgrestResult(bodyOrNull(), headers, postgrest)

}
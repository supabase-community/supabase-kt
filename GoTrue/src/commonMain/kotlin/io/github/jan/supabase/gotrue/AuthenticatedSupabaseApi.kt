package io.github.jan.supabase.gotrue

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.network.SupabaseApi
import io.github.jan.supabase.plugins.MainPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse

class AuthenticatedSupabaseApi(
    resolveUrl: (String) -> String,
    supabaseClient: SupabaseClient,
): SupabaseApi(resolveUrl, supabaseClient) {

    override suspend fun request(url: String, builder: HttpRequestBuilder.() -> Unit): HttpResponse = super.request(url) {
        supabaseClient.gotrue.currentSessionOrNull()?.let {
            headers {
                append("Authorization", "Bearer ${it.accessToken}")
            }
        }
        builder()
    }

}

fun SupabaseClient.authenticatedSupabaseApi(baseUrl: String) = AuthenticatedSupabaseApi({ baseUrl + it }, this)

fun SupabaseClient.authenticatedSupabaseApi(plugin: MainPlugin<*>) = AuthenticatedSupabaseApi(plugin::resolveUrl, this)
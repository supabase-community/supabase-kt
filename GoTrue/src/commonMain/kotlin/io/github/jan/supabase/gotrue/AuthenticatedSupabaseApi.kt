package io.github.jan.supabase.gotrue

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.network.SupabaseHttpClient
import io.github.jan.supabase.plugins.MainPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse

class AuthenticatedSupabaseApi(
    private val resolveUrl: (String) -> String,
    private val supabaseClient: SupabaseClient,
): SupabaseHttpClient() {

    override suspend fun request(url: String, builder: HttpRequestBuilder.() -> Unit): HttpResponse {
        return supabaseClient.httpClient.request(resolveUrl(url)) {
            builder.invoke(this)
            supabaseClient.gotrue.currentSessionOrNull()?.let {
                headers {
                    append("Authorization", "Bearer ${it.accessToken}")
                }
            }
        }
    }

    operator fun plus(endpoint: String) = AuthenticatedSupabaseApi({ resolveUrl(endpoint) + it }, supabaseClient)

}

fun SupabaseClient.authenticatedSupabaseApi(baseUrl: String) = AuthenticatedSupabaseApi({ baseUrl + it }, this)

fun SupabaseClient.authenticatedSupabaseApi(plugin: MainPlugin<*>) = AuthenticatedSupabaseApi(plugin::resolveUrl, this)
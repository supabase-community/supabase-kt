package io.github.jan.supabase.gotrue

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.network.SupabaseApi
import io.github.jan.supabase.plugins.MainPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse

class AuthenticatedSupabaseApi(
    resolveUrl: (path: String) -> String,
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

/**
 * Creates a [AuthenticatedSupabaseApi] with the given [baseUrl]. Requires [GoTrue] to authenticate requests
 * All requests will be resolved relative to this url
 */
fun SupabaseClient.authenticatedSupabaseApi(baseUrl: String) = authenticatedSupabaseApi { baseUrl + it }

/**
 * Creates a [AuthenticatedSupabaseApi] for the given [plugin]. Requires [GoTrue] to authenticate requests
 * All requests will be resolved using the [MainPlugin.resolveUrl] function
 */
fun SupabaseClient.authenticatedSupabaseApi(plugin: MainPlugin<*>) = authenticatedSupabaseApi(plugin::resolveUrl)

/**
 * Creates a [AuthenticatedSupabaseApi] with the given [resolveUrl] function. Requires [GoTrue] to authenticate requests
 * All requests will be resolved using this function
 */
fun SupabaseClient.authenticatedSupabaseApi(resolveUrl: (path: String) -> String) = AuthenticatedSupabaseApi(resolveUrl, this)
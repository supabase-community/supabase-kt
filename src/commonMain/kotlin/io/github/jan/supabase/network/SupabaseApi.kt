package io.github.jan.supabase.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.plugins.MainPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse

open class SupabaseApi(
    private val resolveUrl: (path: String) -> String,
    val supabaseClient: SupabaseClient
) : SupabaseHttpClient() {

    override suspend fun request(url: String, builder: HttpRequestBuilder.() -> Unit): HttpResponse {
        return supabaseClient.httpClient.request(resolveUrl(url), builder)
    }

}

/**
 * Creates a [SupabaseApi] with the given [baseUrl]
 * All requests will be resolved relative to this url
 */
fun SupabaseClient.supabaseApi(baseUrl: String) = supabaseApi { baseUrl + it }

/**
 * Creates a [SupabaseApi] for the given [plugin]
 * All requests will be resolved using the [MainPlugin.resolveUrl] function
 */
fun SupabaseClient.supabaseApi(plugin: MainPlugin<*>) = supabaseApi(plugin::resolveUrl)

/**
 * Creates a [SupabaseApi] with the given [resolveUrl] function
 * All requests will be resolved using this function
 */
fun SupabaseClient.supabaseApi(resolveUrl: (path: String) -> String) = SupabaseApi(resolveUrl, this)
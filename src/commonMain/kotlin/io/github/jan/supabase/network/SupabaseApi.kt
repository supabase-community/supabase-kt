package io.github.jan.supabase.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.plugins.MainPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse

open class SupabaseApi(
    private val resolveUrl: (String) -> String,
    val supabaseClient: SupabaseClient
) : SupabaseHttpClient() {

    override suspend fun request(url: String, builder: HttpRequestBuilder.() -> Unit): HttpResponse {
        return supabaseClient.httpClient.request(resolveUrl(url), builder)
    }

}

fun SupabaseClient.supabaseApi(baseUrl: String) = SupabaseApi({ baseUrl + it }, this)

fun SupabaseClient.supabaseApi(plugin: MainPlugin<*>) = SupabaseApi(plugin::resolveUrl, this)
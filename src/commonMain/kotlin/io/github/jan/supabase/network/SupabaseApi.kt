package io.github.jan.supabase.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.plugins.MainPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement

open class SupabaseApi(
    private val resolveUrl: (path: String) -> String,
    private val parseErrorResponse: (suspend (response: HttpResponse) -> RestException)? = null,
    val supabaseClient: SupabaseClient
) : SupabaseHttpClient() {

    final override suspend fun request(url: String, builder: HttpRequestBuilder.() -> Unit): HttpResponse {
        return rawRequest(resolveUrl(url), builder)
    }

    open suspend fun rawRequest(url: String, builder: HttpRequestBuilder.() -> Unit): HttpResponse {
        return supabaseClient.httpClient.request(url, builder).also {
            if(it.status.value in 400..501 && parseErrorResponse != null) throw parseErrorResponse.invoke(it)
        }
    }

    override suspend fun prepareRequest(
        url: String,
        builder: HttpRequestBuilder.() -> Unit
    ): HttpStatement = supabaseClient.httpClient.prepareRequest(resolveUrl(url), builder)

    suspend fun prepareRequest(builder: HttpRequestBuilder.() -> Unit): HttpStatement = prepareRequest("", builder)

}

/**
 * Creates a [SupabaseApi] with the given [baseUrl]
 * All requests will be resolved relative to this url
 */
fun SupabaseClient.supabaseApi(baseUrl: String, parseErrorResponse: (suspend (response: HttpResponse) -> RestException)? = null) = supabaseApi({ baseUrl + it }, parseErrorResponse)

/**
 * Creates a [SupabaseApi] for the given [plugin]
 * All requests will be resolved using the [MainPlugin.resolveUrl] function
 */
fun SupabaseClient.supabaseApi(plugin: MainPlugin<*>) = supabaseApi(plugin::resolveUrl, plugin::parseErrorResponse)

/**
 * Creates a [SupabaseApi] with the given [resolveUrl] function
 * All requests will be resolved using this function
 */
fun SupabaseClient.supabaseApi(resolveUrl: (path: String) -> String, parseErrorResponse: (suspend (response: HttpResponse) -> RestException)? = null) = SupabaseApi(resolveUrl, parseErrorResponse, this)
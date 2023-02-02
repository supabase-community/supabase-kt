package io.github.jan.supabase.gotrue

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.network.SupabaseApi
import io.github.jan.supabase.plugins.MainPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse

class AuthenticatedSupabaseApi(
    resolveUrl: (path: String) -> String,
    parseErrorResponse: (suspend (response: HttpResponse) -> RestException)? = null,
    supabaseClient: SupabaseClient,
): SupabaseApi(resolveUrl, parseErrorResponse, supabaseClient) {

    override suspend fun rawRequest(url: String, builder: HttpRequestBuilder.() -> Unit): HttpResponse = super.rawRequest(url) {
        supabaseClient.pluginManager.getPluginOrNull(GoTrue)?.currentSessionOrNull()?.let {
            bearerAuth(it.accessToken)
        }
        builder()
    }

}

/**
 * Creates a [AuthenticatedSupabaseApi] with the given [baseUrl]. Requires [GoTrue] to authenticate requests
 * All requests will be resolved relative to this url
 */
fun SupabaseClient.authenticatedSupabaseApi(baseUrl: String, parseErrorResponse: (suspend (response: HttpResponse) -> RestException)? = null) = authenticatedSupabaseApi({ baseUrl + it }, parseErrorResponse)

/**
 * Creates a [AuthenticatedSupabaseApi] for the given [plugin]. Requires [GoTrue] to authenticate requests
 * All requests will be resolved using the [MainPlugin.resolveUrl] function
 */
fun SupabaseClient.authenticatedSupabaseApi(plugin: MainPlugin<*>) = authenticatedSupabaseApi(plugin::resolveUrl, plugin::parseErrorResponse)

/**
 * Creates a [AuthenticatedSupabaseApi] with the given [resolveUrl] function. Requires [GoTrue] to authenticate requests
 * All requests will be resolved using this function
 */
fun SupabaseClient.authenticatedSupabaseApi(resolveUrl: (path: String) -> String, parseErrorResponse: (suspend (response: HttpResponse) -> RestException)? = null) = AuthenticatedSupabaseApi(resolveUrl, parseErrorResponse, this)
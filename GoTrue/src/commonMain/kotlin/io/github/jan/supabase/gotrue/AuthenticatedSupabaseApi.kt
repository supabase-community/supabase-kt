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
    private val jwtToken: String? = null // Can be configured plugin-wide. By default, all plugins use the token from the current session
): SupabaseApi(resolveUrl, parseErrorResponse, supabaseClient) {

    override suspend fun rawRequest(url: String, builder: HttpRequestBuilder.() -> Unit): HttpResponse = super.rawRequest(url) {
        supabaseClient.pluginManager.getPluginOrNull(GoTrue)?.let { gotrue ->
            val jwtToken = jwtToken ?: gotrue.currentAccessTokenOrNull()
            jwtToken?.let { token ->
                bearerAuth(token)
            }
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
fun SupabaseClient.authenticatedSupabaseApi(plugin: MainPlugin<*>) = authenticatedSupabaseApi(plugin::resolveUrl, plugin::parseErrorResponse, plugin.config.jwtToken)

/**
 * Creates a [AuthenticatedSupabaseApi] with the given [resolveUrl] function. Requires [GoTrue] to authenticate requests
 * All requests will be resolved using this function
 */
fun SupabaseClient.authenticatedSupabaseApi(resolveUrl: (path: String) -> String, parseErrorResponse: (suspend (response: HttpResponse) -> RestException)? = null, jwtToken: String? = null) = AuthenticatedSupabaseApi(resolveUrl, parseErrorResponse, this, jwtToken)
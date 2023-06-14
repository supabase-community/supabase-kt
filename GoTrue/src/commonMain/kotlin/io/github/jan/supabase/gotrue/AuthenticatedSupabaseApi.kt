@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")
package io.github.jan.supabase.gotrue

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.network.SupabaseApi
import io.github.jan.supabase.plugins.MainPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement

@OptIn(SupabaseInternal::class)
class AuthenticatedSupabaseApi @SupabaseInternal constructor(
    resolveUrl: (path: String) -> String,
    parseErrorResponse: (suspend (response: HttpResponse) -> RestException)? = null,
    private val defaultRequest: (HttpRequestBuilder.() -> Unit)? = null,
    supabaseClient: SupabaseClient,
    private val jwtToken: String? = null // Can be configured plugin-wide. By default, all plugins use the token from the current session
): SupabaseApi(resolveUrl, parseErrorResponse, supabaseClient) {

    override suspend fun rawRequest(url: String, builder: HttpRequestBuilder.() -> Unit): HttpResponse = super.rawRequest(url) {
        val accessToken = supabaseClient.pluginManager.getPluginOrNull(GoTrue)?.let { gotrue ->
            jwtToken ?: gotrue.currentAccessTokenOrNull()
        }
        bearerAuth(accessToken ?: supabaseClient.supabaseKey)
        builder()
        defaultRequest?.invoke(this)
    }

    suspend fun rawRequest(builder: HttpRequestBuilder.() -> Unit): HttpResponse = rawRequest("", builder)

    override suspend fun prepareRequest(
        url: String,
        builder: HttpRequestBuilder.() -> Unit
    ): HttpStatement {
        return super.prepareRequest(url) {
            supabaseClient.pluginManager.getPluginOrNull(GoTrue)?.let { gotrue ->
                val jwtToken = jwtToken ?: gotrue.currentAccessTokenOrNull() ?: supabaseClient.supabaseKey
                bearerAuth(jwtToken)
            }
            builder()
            defaultRequest?.invoke(this)
        }
    }

}

/**
 * Creates a [AuthenticatedSupabaseApi] with the given [baseUrl]. Requires [GoTrue] to authenticate requests
 * All requests will be resolved relative to this url
 */
@SupabaseInternal
fun SupabaseClient.authenticatedSupabaseApi(baseUrl: String, parseErrorResponse: (suspend (response: HttpResponse) -> RestException)? = null) = authenticatedSupabaseApi({ baseUrl + it }, parseErrorResponse)

/**
 * Creates a [AuthenticatedSupabaseApi] for the given [plugin]. Requires [GoTrue] to authenticate requests
 * All requests will be resolved using the [MainPlugin.resolveUrl] function
 */
@SupabaseInternal
fun SupabaseClient.authenticatedSupabaseApi(plugin: MainPlugin<*>, defaultRequest: (HttpRequestBuilder.() -> Unit)? = null) = authenticatedSupabaseApi(plugin::resolveUrl, plugin::parseErrorResponse, defaultRequest, plugin.config.jwtToken)

/**
 * Creates a [AuthenticatedSupabaseApi] with the given [resolveUrl] function. Requires [GoTrue] to authenticate requests
 * All requests will be resolved using this function
 */
@SupabaseInternal
fun SupabaseClient.authenticatedSupabaseApi(resolveUrl: (path: String) -> String, parseErrorResponse: (suspend (response: HttpResponse) -> RestException)? = null, defaultRequest: (HttpRequestBuilder.() -> Unit)? = null, jwtToken: String? = null) = AuthenticatedSupabaseApi(resolveUrl, parseErrorResponse, defaultRequest, this, jwtToken)
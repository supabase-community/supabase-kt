package io.github.jan.supabase.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.plugins.MainConfig
import io.github.jan.supabase.plugins.MainPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse

/**
 * Creates a [AuthenticatedSupabaseApi] with the given [baseUrl]. Requires [Auth] to authenticate requests
 * All requests will be resolved relative to this url
 */
@SupabaseInternal
fun SupabaseClient.authenticatedSupabaseApi(
    baseUrl: String,
    parseErrorResponse: (suspend (response: HttpResponse) -> RestException)? = null,
    config: AuthenticatedApiConfig
) =
    authenticatedSupabaseApi({ baseUrl + it }, parseErrorResponse, config)

/**
 * Creates a [AuthenticatedSupabaseApi] for the given [plugin]. Requires [Auth] to authenticate requests
 * All requests will be resolved using the [MainPlugin.resolveUrl] function
 */
@SupabaseInternal
fun <C> SupabaseClient.authenticatedSupabaseApi(
    plugin: MainPlugin<C>,
    defaultRequest: (HttpRequestBuilder.() -> Unit)? = null,
    requireSession: Boolean = plugin.config.requireValidSession
): AuthenticatedSupabaseApi where C : MainConfig, C : AuthDependentPluginConfig =
    authenticatedSupabaseApi(plugin::resolveUrl, plugin::parseErrorResponse, AuthenticatedApiConfig(defaultRequest = defaultRequest, requireSession = requireSession, jwtToken = plugin.config.jwtToken))

/**
 * Creates a [AuthenticatedSupabaseApi] with the given [resolveUrl] function. Requires [Auth] to authenticate requests
 * All requests will be resolved using this function
 */
@SupabaseInternal
fun SupabaseClient.authenticatedSupabaseApi(
    resolveUrl: (path: String) -> String,
    parseErrorResponse: (suspend (response: HttpResponse) -> RestException)? = null,
    config: AuthenticatedApiConfig
) =
    AuthenticatedSupabaseApi(resolveUrl, parseErrorResponse, this, config)

@SupabaseInternal
fun AuthenticatedSupabaseApi.withDefaultRequest(builder: HttpRequestBuilder.() -> Unit): AuthenticatedSupabaseApi {
    return AuthenticatedSupabaseApi(
        this.resolveUrl,
        this.parseErrorResponse,
        this.supabaseClient,
        this.config.copy(defaultRequest = {
            this@withDefaultRequest.config.defaultRequest?.invoke(this)
            builder()
        })
    )
}

@SupabaseInternal
fun AuthenticatedSupabaseApi.resolve(path: String): AuthenticatedSupabaseApi {
    return AuthenticatedSupabaseApi(
        { this.resolveUrl("$path/$it") },
        this.parseErrorResponse,
        this.supabaseClient,
        this.config
    )
}
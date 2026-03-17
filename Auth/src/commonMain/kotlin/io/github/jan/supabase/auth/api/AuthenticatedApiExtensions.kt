package io.github.jan.supabase.auth.api

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.AuthDependentPluginConfig
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.plugins.MainConfig
import io.github.jan.supabase.plugins.MainPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse

/**
 * Creates a [AuthenticatedSupabaseApi] with the given [baseUrl]. Requires [Auth] to authenticate requests.
 * All requests will be resolved relative to this url.
 */
@SupabaseInternal
fun SupabaseClient.authenticatedSupabaseApi(
    baseUrl: String,
    parseErrorResponse: (suspend (response: HttpResponse) -> RestException)? = null,
    config: AuthenticatedApiConfig
): AuthenticatedSupabaseApi = authenticatedSupabaseApi(
    resolveUrl = { baseUrl + it },
    parseErrorResponse = parseErrorResponse,
    config = config
)

/**
 * Creates a [AuthenticatedSupabaseApi] with the given [baseUrl]. Requires [Auth] to authenticate requests.
 * All requests will be resolved relative to this url.
 */
@SupabaseInternal
fun SupabaseClient.authenticatedSupabaseApi(
    baseUrl: String,
    parseErrorResponse: (suspend (response: HttpResponse) -> RestException)? = null,
    requireSession: Boolean,
    configure: AuthenticatedApiConfig.Builder.() -> Unit
): AuthenticatedSupabaseApi = authenticatedSupabaseApi(
    resolveUrl = { baseUrl + it },
    parseErrorResponse = parseErrorResponse,
    requireSession = requireSession,
    configure = configure
)

/**
 * Creates a [AuthenticatedSupabaseApi] for the given [plugin]. Requires [Auth] to authenticate requests.
 * All requests will be resolved using the [MainPlugin.resolveUrl] function.
 */
@SupabaseInternal
fun <C> SupabaseClient.authenticatedSupabaseApi(
    plugin: MainPlugin<C>,
    defaultRequest: (HttpRequestBuilder.() -> Unit)? = null,
    requireSession: Boolean = plugin.config.requireValidSession,
    urlLengthLimit: Int? = null
): AuthenticatedSupabaseApi where C : MainConfig, C : AuthDependentPluginConfig =
    authenticatedSupabaseApi(plugin, requireSession) {
        this.defaultRequest = defaultRequest
        this.urlLengthLimit = urlLengthLimit
    }

/**
 * Creates a [AuthenticatedSupabaseApi] for the given [plugin]. Requires [Auth] to authenticate requests.
 * All requests will be resolved using the [MainPlugin.resolveUrl] function.
 */
@SupabaseInternal
fun <C> SupabaseClient.authenticatedSupabaseApi(
    plugin: MainPlugin<C>,
    requireSession: Boolean = plugin.config.requireValidSession,
    configure: AuthenticatedApiConfig.Builder.() -> Unit
): AuthenticatedSupabaseApi where C : MainConfig, C : AuthDependentPluginConfig {
    val client = this
    return authenticatedSupabaseApi(
        resolveUrl = plugin::resolveUrl,
        parseErrorResponse = plugin::parseErrorResponse,
        requireSession = requireSession
    ) {
        jwtToken = plugin.config.jwtToken
        getAccessToken = Auth.defaultResolveAccessToken(client)
        configure()
    }
}

/**
 * Creates a [AuthenticatedSupabaseApi] with the given [resolveUrl] function. Requires [Auth] to authenticate requests.
 * All requests will be resolved using this function.
 */
@SupabaseInternal
fun SupabaseClient.authenticatedSupabaseApi(
    resolveUrl: (path: String) -> String,
    parseErrorResponse: (suspend (response: HttpResponse) -> RestException)? = null,
    config: AuthenticatedApiConfig
): AuthenticatedSupabaseApi = AuthenticatedSupabaseApi(
    httpClient = this.httpClient,
    config = config.withContext(resolveUrl, parseErrorResponse)
)

/**
 * Creates a [AuthenticatedSupabaseApi] with the given [resolveUrl] function. Requires [Auth] to authenticate requests.
 * All requests will be resolved using this function.
 */
@SupabaseInternal
fun SupabaseClient.authenticatedSupabaseApi(
    resolveUrl: (path: String) -> String,
    parseErrorResponse: (suspend (response: HttpResponse) -> RestException)? = null,
    requireSession: Boolean,
    configure: AuthenticatedApiConfig.Builder.() -> Unit
): AuthenticatedSupabaseApi = AuthenticatedSupabaseApi(
    httpClient = this.httpClient,
    config = buildAuthenticatedApiConfig(resolveUrl, parseErrorResponse, requireSession, configure)
)

private fun SupabaseClient.buildAuthenticatedApiConfig(
    resolveUrl: (path: String) -> String,
    parseErrorResponse: (suspend (response: HttpResponse) -> RestException)?,
    requireSession: Boolean,
    configure: AuthenticatedApiConfig.Builder.() -> Unit
): AuthenticatedApiConfig = AuthenticatedApiConfig.Builder().apply {
    this.resolveUrl = resolveUrl
    this.parseErrorResponse = parseErrorResponse
    this.requireSession = requireSession
    this.getAccessToken = Auth.defaultResolveAccessToken(this@buildAuthenticatedApiConfig)
    configure()
}.build()

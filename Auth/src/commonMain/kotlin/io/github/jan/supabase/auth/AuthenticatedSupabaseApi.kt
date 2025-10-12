@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")
package io.github.jan.supabase.auth

import io.github.jan.supabase.OSInformation
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.exception.SessionRequiredException
import io.github.jan.supabase.auth.exception.TokenExpiredException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.logging.e
import io.github.jan.supabase.network.SupabaseApi
import io.github.jan.supabase.plugins.MainConfig
import io.github.jan.supabase.plugins.MainPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import kotlin.time.Clock

@SupabaseInternal
data class AuthenticatedApiConfig(
    val jwtToken: String? = null,
    val defaultRequest: (HttpRequestBuilder.() -> Unit)? = null,
    val requireSession: Boolean
)

@OptIn(SupabaseInternal::class)
class AuthenticatedSupabaseApi @SupabaseInternal constructor(
    resolveUrl: (path: String) -> String,
    parseErrorResponse: (suspend (response: HttpResponse) -> RestException)? = null,
    supabaseClient: SupabaseClient,
    config: AuthenticatedApiConfig
): SupabaseApi(resolveUrl, parseErrorResponse, supabaseClient) {

    private val defaultRequest = config.defaultRequest
    private val jwtToken = config.jwtToken
    private val requireSession = config.requireSession

    override suspend fun rawRequest(url: String, builder: HttpRequestBuilder.() -> Unit): HttpResponse {
        val builder = HttpRequestBuilder().apply(builder)
        val accessToken = supabaseClient.resolveAccessToken(jwtToken, keyAsFallback = !requireSession)
            ?: throw SessionRequiredException()
        checkAccessToken(accessToken)
        return super.rawRequest(url) {
            bearerAuth(accessToken)
            defaultRequest?.invoke(this)
            this
        }
    }

    suspend fun rawRequest(builder: HttpRequestBuilder.() -> Unit): HttpResponse = rawRequest("", builder)

    override suspend fun prepareRequest(
        url: String,
        builder: HttpRequestBuilder.() -> Unit
    ): HttpStatement {
        val accessToken = supabaseClient.resolveAccessToken(jwtToken, keyAsFallback = !requireSession)
            ?: throw SessionRequiredException()
        checkAccessToken(accessToken)
        return super.prepareRequest(url) {
            bearerAuth(accessToken)
            builder()
            defaultRequest?.invoke(this)
        }
    }

    private suspend fun checkAccessToken(token: String?) {
        val currentSession = supabaseClient.auth.currentSessionOrNull()
        val now = Clock.System.now()
        val sessionExistsAndExpired =
            token == currentSession?.accessToken && currentSession != null && currentSession.expiresAt < now
        val autoRefreshEnabled = supabaseClient.auth.config.alwaysAutoRefresh
        if (sessionExistsAndExpired && autoRefreshEnabled) {
            val autoRefreshRunning = supabaseClient.auth.isAutoRefreshRunning
            Auth.logger.e {
                """
                Authenticated request attempted with expired access token. This should not happen. Please report this issue. Trying to refresh session before...
                Auto refresh running: $autoRefreshRunning
                OS: ${OSInformation.CURRENT}
                Session: $currentSession
            """.trimIndent()
            }

            try {
                supabaseClient.auth.refreshCurrentSession()
            } catch (e: Exception) {
                Auth.logger.e(e) { "Failed to refresh session" }
                throw TokenExpiredException()
            }
        }
    }

}

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
    authenticatedSupabaseApi(plugin::resolveUrl, plugin::parseErrorResponse, AuthenticatedApiConfig(defaultRequest = defaultRequest, requireSession = requireSession))

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
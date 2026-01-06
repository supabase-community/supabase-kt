package io.github.jan.supabase.auth

import io.github.jan.supabase.OSInformation
import io.github.jan.supabase.StringMasking
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.exception.TokenExpiredException
import io.github.jan.supabase.logging.e
import io.github.jan.supabase.plugins.MainConfig
import io.github.jan.supabase.plugins.MainPlugin
import kotlin.time.Clock

/**
 * Returns the access token used for requests. The token is resolved in the following order:
 * 1. [jwtToken] if not null
 * 2. [SupabaseClient.accessToken] if not null
 * 3. [Auth.currentAccessTokenOrNull] if the Auth plugin is installed. This method also checks if the token expired and tries to force-refresh it.
 * 4. [SupabaseClient.supabaseKey] if [keyAsFallback] is true
 */
@SupabaseInternal
suspend fun SupabaseClient.resolveAccessToken(
    jwtToken: String? = null,
    keyAsFallback: Boolean = true
): String? {
    val key = if(keyAsFallback) supabaseKey else null
    return jwtToken ?: accessToken?.invoke()
    ?: pluginManager.getPluginOrNull(Auth)?.currentAccessTokenOrNull()?.also { checkAccessToken(it) } ?: key
}

/**
 * Returns the access token used for requests. The token is resolved in the following order:
 * 1. [MainConfig.jwtToken] if not null
 * 2. [SupabaseClient.resolveAccessToken] if not null
 * 3. [Auth.currentAccessTokenOrNull] if the Auth plugin is installed. This method also checks if the token expired and tries to force-refresh it.
 * 4. [SupabaseClient.supabaseKey] if [keyAsFallback] is true
 */
@SupabaseInternal
suspend fun <C : MainConfig> SupabaseClient.resolveAccessToken(
    plugin: MainPlugin<C>,
    keyAsFallback: Boolean = true
) = resolveAccessToken(plugin.config.jwtToken, keyAsFallback)

private suspend fun SupabaseClient.checkAccessToken(token: String) {
    val auth = pluginManager.getPluginOrNull(Auth) ?: return
    val currentSession = auth.currentSessionOrNull()
    val now = Clock.System.now()
    val sessionExistsAndExpired =
        token == currentSession?.accessToken && currentSession.expiresAt < now
    val autoRefreshEnabled = auth.config.alwaysAutoRefresh
    if (sessionExistsAndExpired && autoRefreshEnabled) {
        val autoRefreshRunning = auth.isAutoRefreshRunning
        Auth.logger.e {
            """
                Authenticated request attempted with expired access token. This should not happen. Please report this issue. Trying to refresh session before...
                Auto refresh running: $autoRefreshRunning
                OS: ${OSInformation.CURRENT}
                Session: ${StringMasking.maskSession(currentSession)}
            """.trimIndent()
        }

        try {
            auth.refreshCurrentSession()
        } catch (e: Exception) {
            Auth.logger.e(e) { "Failed to force-refresh session before making a request with an expired access token" }
            throw TokenExpiredException()
        }
    }
}
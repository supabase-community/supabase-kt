package io.github.jan.supabase.gotrue.providers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.user.UserSession
import platform.Foundation.NSURL

/**
 * Represents an OAuth provider.
 */
actual abstract class OAuthProvider : AuthProvider<ExternalAuthConfig, Unit> {

    /**
     * The name of the provider.
     */
    actual abstract val name: String

    actual override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        val externalConfig = ExternalAuthConfig().apply(config ?: {})
        openOAuth(redirectUrl, supabaseClient, externalConfig)
    }

    actual override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        val externalConfig = ExternalAuthConfig().apply(config ?: {})
        openOAuth(redirectUrl, supabaseClient, externalConfig)
    }

    private fun openOAuth(
        redirectUrl: String? = null,
        supabaseClient: SupabaseClient,
        externalConfig: ExternalAuthConfig
    ) {
        val gotrue = supabaseClient.gotrue
        val deepLink = "${gotrue.config.scheme}://${gotrue.config.host}"
        val url = NSURL(string = supabaseClient.gotrue.oAuthUrl(this, redirectUrl ?: deepLink) {
            scopes.addAll(externalConfig.scopes)
            queryParams.putAll(externalConfig.queryParams)
        })
        openUrl(url)
    }

    actual companion object

}

internal expect fun openUrl(url: NSURL)
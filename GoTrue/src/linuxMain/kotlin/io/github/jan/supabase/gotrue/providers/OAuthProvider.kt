package io.github.jan.supabase.gotrue.providers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserSession
import platform.posix.system

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
        val externalConfig = ExternalAuthConfig().apply { config?.invoke(this) }
        val url = supabaseClient.auth.oAuthUrl(this@OAuthProvider, redirectUrl) {
            scopes.addAll(externalConfig.scopes)
            queryParams.putAll(externalConfig.queryParams)
        }
        system("xdg-open $url")
    }

    actual override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) = login(supabaseClient, onSuccess, redirectUrl, config = config)

    actual companion object

}
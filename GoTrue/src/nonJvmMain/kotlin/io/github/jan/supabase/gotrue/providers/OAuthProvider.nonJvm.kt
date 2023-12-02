package io.github.jan.supabase.gotrue.providers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.openExternalUrl
import io.github.jan.supabase.gotrue.user.UserSession

/**
 * Represents an OAuth provider.
 */
actual abstract class OAuthProvider actual constructor() : AuthProvider<ExternalAuthConfig, Unit> {
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
        val builtConfig = ExternalAuthConfig().apply {
            config?.invoke(this)
        }
        val url = supabaseClient.auth.oAuthUrl(this, redirectUrl) {
            scopes.addAll(builtConfig.scopes)
            queryParams.putAll(builtConfig.queryParams)
        }
        supabaseClient.openExternalUrl(url)
    }

    actual override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) = login(supabaseClient, onSuccess, redirectUrl, config)

    actual companion object

}
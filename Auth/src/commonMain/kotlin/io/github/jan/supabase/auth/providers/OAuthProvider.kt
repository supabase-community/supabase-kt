package io.github.jan.supabase.auth.providers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.startExternalAuth
import io.github.jan.supabase.auth.user.UserSession

/**
 * Represents an OAuth provider.
 */
abstract class OAuthProvider : AuthProvider<ExternalAuthConfig, Unit> {

    /**
     * The name of the provider.
     */
    abstract val name: String

    override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        val authConfig = ExternalAuthConfig()
        if (config != null) {
            authConfig.config()
        }
        supabaseClient.auth.startExternalAuth(
            redirectUrl = redirectUrl,
            getUrl = {
                supabaseClient.auth.getOAuthUrl(this, it) {
                    scopes.addAll(authConfig.scopes)
                    queryParams.putAll(authConfig.queryParams)
                }
            },
            onSessionSuccess = onSuccess
        )
    }

    override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) = login(supabaseClient, onSuccess, redirectUrl, config)

    companion object

}
package io.github.jan.supabase.gotrue.providers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.AuthImpl
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.deepLink
import io.github.jan.supabase.gotrue.openOAuth
import io.github.jan.supabase.gotrue.user.UserSession

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
        val gotrue = supabaseClient.auth as AuthImpl
        val redirectTo = redirectUrl ?: gotrue.config.deepLink
        val externalConfig = ExternalAuthConfig().apply { config?.invoke(this) }
        gotrue.openOAuth(this, redirectTo, externalConfig)
    }

    actual override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        login(supabaseClient, onSuccess, redirectUrl, config)
    }

    actual companion object


}
package io.github.jan.supabase.gotrue.providers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.GoTrueImpl
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.openOAuth
import io.github.jan.supabase.gotrue.user.UserSession


actual abstract class OAuthProvider : AuthProvider<ExternalAuthConfig, Unit> {

    actual abstract val name: String

    actual override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        val gotrue = supabaseClient.gotrue as GoTrueImpl
        val deepLink = "${gotrue.config.scheme}://${gotrue.config.host}"
        val externalConfig = ExternalAuthConfig().apply { config?.invoke(this) }
        gotrue.openOAuth(this, redirectUrl ?: deepLink, externalConfig)
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
package io.github.jan.supabase.gotrue.providers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.GoTrueImpl
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.host
import io.github.jan.supabase.gotrue.openOAuth
import io.github.jan.supabase.gotrue.scheme
import io.github.jan.supabase.gotrue.user.UserSession


actual abstract class OAuthProvider : AuthProvider<ExternalAuthConfig, Unit> {

    actual abstract fun provider(): String

    actual override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        val gotrue = supabaseClient.gotrue as GoTrueImpl
        val deepLink = "${gotrue.config.scheme}://${gotrue.config.host}"
        gotrue.openOAuth(provider(), redirectUrl ?: deepLink)
    }

    actual override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        val gotrue = supabaseClient.gotrue as GoTrueImpl
        val deepLink = "${gotrue.config.scheme}://${gotrue.config.host}"
        gotrue.openOAuth(provider(), redirectUrl ?: deepLink)
    }

    actual companion object


}
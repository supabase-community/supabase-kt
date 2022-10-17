package io.github.jan.supabase.auth.providers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.GoTrueImpl
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.openOAuth
import io.github.jan.supabase.auth.user.UserSession


actual abstract class OAuthProvider : AuthProvider<ExternalAuthConfig, Unit> {

    actual abstract fun provider(): String

    actual override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        val auth = supabaseClient.auth as GoTrueImpl
        auth.openOAuth(provider())
    }

    actual override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        val auth = supabaseClient.auth as GoTrueImpl
        auth.openOAuth(provider())
    }


}
package io.github.jan.supacompose.auth.providers

import io.github.jan.supacompose.auth.AuthImpl
import io.github.jan.supacompose.auth.auth
import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.openOAuth
import io.github.jan.supacompose.auth.user.UserSession


actual abstract class OAuthProvider : AuthProvider<ExternalAuthConfig, Unit> {

    actual abstract fun provider(): String

    actual override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        val auth = supabaseClient.auth as AuthImpl
        auth.openOAuth(provider())
    }

    actual override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        val auth = supabaseClient.auth as AuthImpl
        auth.openOAuth(provider())
    }


}
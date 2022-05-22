package io.github.jan.supacompose.auth.providers

import io.github.jan.supacompose.auth.user.UserSession
import io.github.jan.supacompose.SupabaseClient

actual abstract class OAuthProvider : AuthProvider<ExternalAuthConfig, Unit> {

    actual abstract fun provider(): String

    actual override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (AuthFail) -> Unit,
        credentials: (ExternalAuthConfig.() -> Unit)?
    ) {
        TODO()
    }

    actual override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (AuthFail) -> Unit,
        credentials: (ExternalAuthConfig.() -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

}
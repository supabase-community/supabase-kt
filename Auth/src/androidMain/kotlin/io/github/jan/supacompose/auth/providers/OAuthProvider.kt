package io.github.jan.supacompose.auth.providers

import io.github.jan.supacompose.auth.user.UserSession

actual open class OAuthProvider : AuthProvider<ExternalAuthConfig, Unit> {

    actual override suspend fun login(
        supabaseClient: io.github.jan.supacompose.SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (OAuthFail) -> Unit,
        credentials: (ExternalAuthConfig.() -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    actual override suspend fun signUp(
        supabaseClient: io.github.jan.supacompose.SupabaseClient,
        credentials: ExternalAuthConfig.() -> Unit
    ) {
        TODO("Not yet implemented")
    }


}
package io.github.jan.supacompose.auth.providers

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.user.UserSession

expect abstract class OAuthProvider() : AuthProvider<ExternalAuthConfig, Unit> {

    abstract fun provider(): String

    override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (AuthFail) -> Unit,
        config: (ExternalAuthConfig.() -> Unit)?
    )

    override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (AuthFail) -> Unit,
        config: (ExternalAuthConfig.() -> Unit)?
    )

}



sealed interface AuthFail {

    object Timeout: AuthFail
    class Error(val throwable: Throwable) : AuthFail
    object InvalidCredentials : AuthFail

}


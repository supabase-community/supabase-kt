package io.github.jan.supabase.auth.providers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.user.UserSession

expect abstract class OAuthProvider() : AuthProvider<ExternalAuthConfig, Unit> {

    abstract fun provider(): String

    override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    )

    override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    )

}



sealed interface AuthFail {

    object Timeout: AuthFail

}


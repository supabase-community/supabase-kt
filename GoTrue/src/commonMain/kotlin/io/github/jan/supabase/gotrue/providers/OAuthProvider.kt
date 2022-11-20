package io.github.jan.supabase.gotrue.providers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.user.UserSession

expect abstract class OAuthProvider() : AuthProvider<ExternalAuthConfig, Unit> {

    abstract val name: String

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

    companion object

}

sealed interface AuthFail {

    object Timeout: AuthFail

}


package io.github.jan.supacompose.auth.providers

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.user.UserSession

expect abstract class OAuthProvider() : AuthProvider<ExternalAuthConfig, Unit> {

    abstract fun provider(): String

    override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (OAuthFail) -> Unit,
        credentials: (ExternalAuthConfig.() -> Unit)?
    )

    override suspend fun signUp(supabaseClient: SupabaseClient, credentials: ExternalAuthConfig.() -> Unit)

}



sealed interface OAuthFail {

    object Timeout: OAuthFail
    class Error(val throwable: Throwable) : OAuthFail

}

object Google : OAuthProvider() {

    override fun provider() = "google"

}

object Discord : OAuthProvider() {

    override fun provider() = "discord"

}
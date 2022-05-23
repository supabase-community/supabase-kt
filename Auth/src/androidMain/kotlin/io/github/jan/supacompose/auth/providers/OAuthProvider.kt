package io.github.jan.supacompose.auth.providers

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.DeepLinks
import io.github.jan.supacompose.auth.user.UserSession


actual abstract class OAuthProvider : AuthProvider<ExternalAuthConfig, Unit> {

    actual abstract fun provider(): String

    actual override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (AuthFail) -> Unit,
        credentials: (ExternalAuthConfig.() -> Unit)?
    ) {
        (supabaseClient.plugins["deeplinks"] as? DeepLinks ?: throw IllegalStateException("You need to install the android plugin on supabase client, call the initial method and call the handleDeepLink method on the supabase client")).openOAuth(provider())
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
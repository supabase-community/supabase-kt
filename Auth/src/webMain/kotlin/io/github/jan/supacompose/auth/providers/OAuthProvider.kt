package io.github.jan.supacompose.auth.providers

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.user.UserSession
import kotlinx.browser.window

actual abstract class OAuthProvider actual constructor() : AuthProvider<ExternalAuthConfig, Unit> {

    actual abstract fun provider(): String

    actual override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (AuthFail) -> Unit,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        val authConfig = ExternalAuthConfig().apply(config ?: throw IllegalStateException("No redirect url provided"))
        window.location.href = supabaseClient.supabaseUrl + "/auth/v1/auth/v1/authorize?provider=${provider()}&redirect_to=${authConfig.redirectUrl}"
    }

    actual override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (AuthFail) -> Unit,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        val authConfig = ExternalAuthConfig().apply(config ?: throw IllegalStateException("No redirect url provided"))
        window.location.href = supabaseClient.supabaseUrl + "/auth/v1/auth/v1/authorize?provider=${provider()}&redirect_to=${authConfig.redirectUrl}"
    }

}

var ExternalAuthConfig.redirectUrl: String
    get() = params["redirectUrl"] as? String ?: ""
    set(value) {
        params["redirectUrl"] = value
    }
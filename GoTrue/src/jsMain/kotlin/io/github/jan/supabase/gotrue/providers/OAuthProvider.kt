package io.github.jan.supabase.gotrue.providers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.browser.window

actual abstract class OAuthProvider actual constructor() : AuthProvider<ExternalAuthConfig, Unit> {

    actual abstract val name: String

    actual override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        val authConfig = ExternalAuthConfig().apply {
            config?.invoke(this)
        }
        window.location.href = supabaseClient.supabaseHttpUrl + "/${GoTrue.key}/v${GoTrue.API_VERSION}/authorize?provider=$name&redirect_to=${redirectUrl ?: authConfig.redirectUrl}"
    }

    actual override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        login(supabaseClient, onSuccess, redirectUrl, config)
    }

    actual companion object

}
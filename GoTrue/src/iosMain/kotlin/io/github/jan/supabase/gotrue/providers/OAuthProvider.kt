package io.github.jan.supabase.gotrue.providers

import io.github.aakira.napier.Napier
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.user.UserSession
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual abstract class OAuthProvider : AuthProvider<ExternalAuthConfig, Unit> {

    actual abstract val name: String

    actual override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        openOAuth(redirectUrl, supabaseClient)
    }

    actual override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        openOAuth(redirectUrl, supabaseClient)
    }

    private fun openOAuth(redirectUrl: String? = null, supabaseClient: SupabaseClient) {
        val gotrue = supabaseClient.gotrue
        val deepLink = "${gotrue.config.scheme}://${gotrue.config.host}"
        val url = NSURL(string = supabaseClient.gotrue.resolveUrl("authorize?provider=$name&redirect_to=${redirectUrl ?: deepLink}"))
        UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>()) {
            if(it) Napier.d { "Successfully opened provider url in safari" } else Napier.e { "Failed to open provider url in safari" }
        }
    }

    actual companion object

}
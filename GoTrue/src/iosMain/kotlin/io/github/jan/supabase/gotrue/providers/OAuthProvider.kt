package io.github.jan.supabase.gotrue.providers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.host
import io.github.jan.supabase.gotrue.scheme
import io.github.jan.supabase.gotrue.user.UserSession
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual abstract class OAuthProvider : AuthProvider<ExternalAuthConfig, Unit> {

    actual abstract fun provider(): String

    actual override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        val gotrue = supabaseClient.gotrue
        val deepLink = "${gotrue.config.scheme}://${gotrue.config.host}"
        val url = NSURL(supabaseClient.gotrue.resolveUrl("authorize?provider=${provider()}&redirect_to=${redirectUrl ?: deepLink}"))
        UIApplication.sharedApplication.openURL(url)
    }

    actual override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        val gotrue = supabaseClient.gotrue
        val deepLink = "${gotrue.config.scheme}://${gotrue.config.host}"
        val url = NSURL(supabaseClient.gotrue.resolveUrl("authorize?provider=${provider()}&redirect_to=${redirectUrl ?: deepLink}"))
        UIApplication.sharedApplication.openURL(url)
    }

    actual companion object

}
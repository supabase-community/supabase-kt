package io.github.jan.supabase.gotrue.providers.builtin

import co.touchlab.kermit.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.user.UserSession
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

internal actual suspend fun <Config : SSO.Config> SSO<Config>.loginWithSSO(
    supabaseClient: SupabaseClient,
    onSuccess: suspend (UserSession) -> Unit,
    redirectUrl: String?,
    config: (Config.() -> Unit)?
) {
    val gotrue = supabaseClient.gotrue
    val deepLink = redirectUrl ?: "${gotrue.config.scheme}://${gotrue.config.host}"
    val result = supabaseClient.gotrue.retrieveSSOUrl(this@loginWithSSO, deepLink, config)
    val url = NSURL(string = result.url)
    UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>()) {
        if(it) Logger.d { "Successfully opened provider url in safari" } else Logger.e { "Failed to open provider url in safari" }
    }
}
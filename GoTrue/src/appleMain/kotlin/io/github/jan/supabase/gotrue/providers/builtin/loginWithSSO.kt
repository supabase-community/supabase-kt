package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.deepLink
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.openUrl
import io.github.jan.supabase.gotrue.user.UserSession
import platform.Foundation.NSURL

internal actual suspend fun <Config : SSO.Config> SSO<Config>.loginWithSSO(
    supabaseClient: SupabaseClient,
    onSuccess: suspend (UserSession) -> Unit,
    redirectUrl: String?,
    config: (Config.() -> Unit)?
) {
    val gotrue = supabaseClient.gotrue
    val deepLink = redirectUrl ?: gotrue.config.deepLink
    val result = supabaseClient.gotrue.retrieveSSOUrl(this@loginWithSSO, deepLink, config)
    val url = NSURL(string = result.url)
    openUrl(url)
}
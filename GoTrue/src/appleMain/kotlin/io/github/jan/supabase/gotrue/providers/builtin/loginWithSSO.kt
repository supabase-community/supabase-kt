package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.deepLink
import io.github.jan.supabase.gotrue.providers.openUrl
import io.github.jan.supabase.gotrue.user.UserSession
import platform.Foundation.NSURL

internal actual suspend fun <Config : SSO.Config> SSO<Config>.loginWithSSO(
    supabaseClient: SupabaseClient,
    onSuccess: suspend (UserSession) -> Unit,
    redirectUrl: String?,
    config: (Config.() -> Unit)?
) {
    val gotrue = supabaseClient.auth
    val result = supabaseClient.auth.retrieveSSOUrl(this@loginWithSSO, redirectUrl ?: gotrue.config.deepLink, config)
    val url = NSURL(string = result.url)
    openUrl(url)
}
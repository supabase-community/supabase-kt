package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserSession
import platform.posix.system

internal actual suspend fun <Config : SSO.Config> SSO<Config>.loginWithSSO(
    supabaseClient: SupabaseClient,
    onSuccess: suspend (UserSession) -> Unit,
    redirectUrl: String?,
    config: (Config.() -> Unit)?
) {
    val result = supabaseClient.auth.retrieveSSOUrl(this@loginWithSSO, redirectUrl, config)
    system("xdg-open ${result.url}")
}
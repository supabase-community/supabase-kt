package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.user.UserSession
import platform.windows.SW_SHOWNORMAL
import platform.windows.ShellExecuteW

internal actual suspend fun <Config : SSO.Config> SSO<Config>.loginWithSSO(
    supabaseClient: SupabaseClient,
    onSuccess: suspend (UserSession) -> Unit,
    redirectUrl: String?,
    config: (Config.() -> Unit)?
) {
    val result = supabaseClient.gotrue.retrieveSSOUrl(this@loginWithSSO, redirectUrl, config)
    ShellExecuteW(null, "open", result.url, null, null, SW_SHOWNORMAL);
}
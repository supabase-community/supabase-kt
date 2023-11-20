package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.cinterop.ExperimentalForeignApi
import platform.windows.SW_SHOWNORMAL
import platform.windows.ShellExecuteW

@OptIn(ExperimentalForeignApi::class)
internal actual suspend fun SSO.loginWithSSO(
    supabaseClient: SupabaseClient,
    onSuccess: suspend (UserSession) -> Unit,
    redirectUrl: String?,
    config: (SSO.Config.() -> Unit)?
) {
    val result = supabaseClient.auth.retrieveSSOUrl(redirectUrl) { config?.invoke(this) }
    ShellExecuteW(null, "open", result.url, null, null, SW_SHOWNORMAL);
}
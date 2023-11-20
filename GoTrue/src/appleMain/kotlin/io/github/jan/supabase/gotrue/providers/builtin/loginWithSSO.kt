package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.deepLink
import io.github.jan.supabase.gotrue.providers.openUrl
import io.github.jan.supabase.gotrue.user.UserSession
import platform.Foundation.NSURL

internal actual suspend fun SSO.loginWithSSO(
    supabaseClient: SupabaseClient,
    onSuccess: suspend (UserSession) -> Unit,
    redirectUrl: String?,
    config: (SSO.Config.() -> Unit)?
) {
    val auth = supabaseClient.auth
    val result = supabaseClient.auth.retrieveSSOUrl(redirectUrl ?: auth.config.deepLink) { config?.invoke(this) }
    val url = NSURL(string = result.url)
    openUrl(url)
}
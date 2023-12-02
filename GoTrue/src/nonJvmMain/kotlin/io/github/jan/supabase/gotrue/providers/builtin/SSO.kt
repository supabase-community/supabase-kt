package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.openExternalUrl
import io.github.jan.supabase.gotrue.user.UserSession

internal actual suspend fun SSO.loginWithSSO(
    supabaseClient: SupabaseClient,
    onSuccess: suspend (UserSession) -> Unit,
    redirectUrl: String?,
    config: (SSO.Config.() -> Unit)?
) {
    val result = supabaseClient.auth.retrieveSSOUrl(redirectUrl) { config?.invoke(this) }
    supabaseClient.openExternalUrl(result.url)
}
package io.github.jan.supabase.gotrue.providers.builtin

import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.deepLink
import io.github.jan.supabase.gotrue.openUrl
import io.github.jan.supabase.gotrue.user.UserSession

internal actual suspend fun SSO.loginWithSSO(
    supabaseClient: SupabaseClient,
    onSuccess: suspend (UserSession) -> Unit,
    redirectUrl: String?,
    config: (SSO.Config.() -> Unit)?
) {
    val gotrueConfig = supabaseClient.auth.config
    val result = supabaseClient.auth.retrieveSSOUrl(redirectUrl ?: gotrueConfig.deepLink) { config?.invoke(this) }
    openUrl(Uri.parse(result.url), gotrueConfig.defaultExternalAuthAction)
}
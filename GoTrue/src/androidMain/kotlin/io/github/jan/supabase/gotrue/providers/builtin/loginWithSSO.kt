package io.github.jan.supabase.gotrue.providers.builtin

import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.openUrl
import io.github.jan.supabase.gotrue.user.UserSession

internal actual suspend fun <Config : SSO.Config> SSO<Config>.loginWithSSO(
    supabaseClient: SupabaseClient,
    onSuccess: suspend (UserSession) -> Unit,
    redirectUrl: String?,
    config: (Config.() -> Unit)?
) {
    val gotrueConfig = supabaseClient.gotrue.config
    val result = supabaseClient.gotrue.retrieveSSOUrl(this, redirectUrl ?: "${gotrueConfig.scheme}://${gotrueConfig.host}", config)
    openUrl(Uri.parse(result.url), gotrueConfig.defaultOAuthAction)
}
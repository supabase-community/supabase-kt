package io.github.jan.supabase.auth.native.external

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.native.platformConfig
import io.github.jan.supabase.auth.user.UserSession

internal actual suspend fun Auth.startOAuthSession(
    redirectUrl: String?,
    getUrl: suspend (redirectTo: String?) -> String,
    onSessionSuccess: suspend (UserSession) -> Unit
) {
    config.platformConfig().urlLauncher.openUrl(supabaseClient, getUrl(redirectUrl))
}
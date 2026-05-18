package io.github.jan.supabase.auth.native.oauth

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.native.platformConfig

internal actual suspend fun Auth.startOAuthSession(
    redirectUrl: String?,
    getUrl: suspend (redirectTo: String?) -> String,
    onSessionSuccess: suspend (io.github.jan.supabase.auth.user.UserSession) -> Unit
) {
    config.platformConfig()?.urlLauncher?.openUrl(supabaseClient, getUrl(redirectUrl)) ?: error("Auth Native not initialized")
}
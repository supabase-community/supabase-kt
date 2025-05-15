@file:Suppress("RedundantSuspendModifier")
package io.github.jan.supabase.auth

import io.github.jan.supabase.auth.user.UserSession

internal actual suspend fun Auth.startExternalAuth(
    redirectUrl: String?,
    getUrl: suspend (redirectTo: String?) -> String,
    onSessionSuccess: suspend (UserSession) -> Unit
) {
    config.urlLauncher.openUrl(supabaseClient, getUrl(redirectUrl))
}
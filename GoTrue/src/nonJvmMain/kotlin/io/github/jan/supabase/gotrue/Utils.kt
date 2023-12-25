@file:Suppress("RedundantSuspendModifier")
package io.github.jan.supabase.gotrue

import io.github.jan.supabase.gotrue.user.UserSession

internal actual suspend fun Auth.startExternalAuth(
    redirectUrl: String?,
    getUrl: suspend (redirectTo: String?) -> String,
    onSessionSuccess: suspend (UserSession) -> Unit
) {
    supabaseClient.openExternalUrl(getUrl(redirectUrl))
}
@file:Suppress("RedundantSuspendModifier")

package io.supabase.auth

import io.supabase.auth.Auth
import io.supabase.auth.user.UserSession
import io.supabase.auth.openExternalUrl

internal actual suspend fun Auth.startExternalAuth(
    redirectUrl: String?,
    getUrl: suspend (redirectTo: String?) -> String,
    onSessionSuccess: suspend (UserSession) -> Unit
) {
    supabaseClient.openExternalUrl(getUrl(redirectUrl))
}
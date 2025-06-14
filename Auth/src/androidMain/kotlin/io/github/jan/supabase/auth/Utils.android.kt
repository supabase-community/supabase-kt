@file:Suppress("RedundantSuspendModifier")
package io.github.jan.supabase.auth

import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.user.UserSession

internal actual suspend fun SupabaseClient.openExternalUrl(url: String) {
    openUrl(Uri.parse(url), auth.config.defaultExternalAuthAction)
}

internal actual suspend fun Auth.startExternalAuth(
    redirectUrl: String?,
    getUrl: suspend (redirectTo: String?) -> String,
    onSessionSuccess: suspend (UserSession) -> Unit
) {
    config.urlLauncher.openUrl(supabaseClient, getUrl(redirectUrl))
}
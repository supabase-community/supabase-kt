@file:Suppress("RedundantSuspendModifier")
package io.github.jan.supabase.auth

import android.content.Intent
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.user.UserSession

internal actual suspend fun SupabaseClient.openExternalUrl(url: String) {
    val context = applicationContext()
    val intent = Intent(context, SupabaseAuthDispatcherActivity::class.java).apply {
        putExtra("auth_url", Uri.parse(url))
        putExtra("action", auth.config.defaultExternalAuthAction)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

internal actual suspend fun Auth.startExternalAuth(
    redirectUrl: String?,
    getUrl: suspend (redirectTo: String?) -> String,
    onSessionSuccess: suspend (UserSession) -> Unit
) {
    config.urlLauncher.openUrl(supabaseClient, getUrl(redirectUrl))
}
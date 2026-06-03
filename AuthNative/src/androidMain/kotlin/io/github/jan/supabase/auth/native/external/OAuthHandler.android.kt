package io.github.jan.supabase.auth.native.external

import android.content.Intent
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.native.applicationContext
import io.github.jan.supabase.auth.native.external.activities.SupabaseAuthDispatcherActivity
import io.github.jan.supabase.auth.native.platformConfig
import io.github.jan.supabase.auth.user.UserSession

internal actual suspend fun SupabaseClient.openExternalUrl(url: String) {
    val context = applicationContext()
    val intent = Intent(context, SupabaseAuthDispatcherActivity::class.java).apply {
        putExtra("auth_url", Uri.parse(url))
        putExtra("action", auth.config.platformConfig().defaultExternalAuthAction)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

internal actual suspend fun Auth.startOAuthSession(
    redirectUrl: String?,
    getUrl: suspend (redirectTo: String?) -> String,
    onSessionSuccess: suspend (UserSession) -> Unit
) {
    config.platformConfig().urlLauncher.openUrl(supabaseClient, getUrl(redirectUrl))
}
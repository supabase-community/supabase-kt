package io.github.jan.supabase.gotrue

import android.content.Intent
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.user.UserSession

internal fun GoTrue.openOAuth(provider: String, redirectTo: String) {
    this as GoTrueImpl
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(resolveUrl("authorize?provider=${provider}&redirect_to=$redirectTo")))
    browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    applicationContext().startActivity(browserIntent)
}

var GoTrue.Config.scheme: String
    get() = (params["scheme"] as? String) ?: "supabase"
    set(value) {
        params["scheme"] = value
    }

var GoTrue.Config.host: String
    get() = (params["host"] as? String) ?: "login"
    set(value) {
        params["host"] = value
    }


//TODO: Add context receiver 'Activity'
fun SupabaseClient.handleDeeplinks(intent: Intent, onSessionSuccess: (UserSession) -> Unit = {}) {
    val authPlugin = pluginManager.getPlugin(GoTrue)
    val data = intent.data ?: return
    val scheme = data.scheme ?: return
    val host = data.host ?: return
    if(scheme != authPlugin.config.scheme || host != authPlugin.config.host) return
    val fragment = data.fragment ?: return
    parseFragment(fragment, onSessionSuccess)
}
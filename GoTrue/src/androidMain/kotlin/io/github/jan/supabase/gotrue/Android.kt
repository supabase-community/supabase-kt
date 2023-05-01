package io.github.jan.supabase.gotrue

import android.content.Intent
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotiations.SupabaseExperimental
import io.github.jan.supabase.gotrue.providers.ExternalAuthConfigDefaults
import io.github.jan.supabase.gotrue.providers.OAuthProvider
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal fun GoTrue.openOAuth(provider: OAuthProvider, redirectTo: String, config: ExternalAuthConfigDefaults) {
    this as GoTrueImpl
    openUrl(Uri.parse(oAuthUrl(provider, redirectTo) {
        scopes.addAll(config.scopes)
        queryParams.putAll(config.queryParams)
    }))
}

internal fun openUrl(uri: Uri) {
    val browserIntent = Intent(Intent.ACTION_VIEW, uri)
    browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    applicationContext().startActivity(browserIntent)
}

//TODO: Add context receiver 'Activity'
@OptIn(SupabaseExperimental::class)
fun SupabaseClient.handleDeeplinks(intent: Intent, onSessionSuccess: (UserSession) -> Unit = {}) {
    val authPlugin = pluginManager.getPlugin(GoTrue)
    val data = intent.data ?: return
    val scheme = data.scheme ?: return
    val host = data.host ?: return
    if(scheme != authPlugin.config.scheme || host != authPlugin.config.host) return
    when(gotrue.config.flowType) {
        FlowType.IMPLICIT -> {
            val fragment = data.fragment ?: return
            parseFragment(fragment, onSessionSuccess)
        }
        FlowType.PKCE -> {
            val code = data.getQueryParameter("code") ?: return
            val scope = CoroutineScope(Dispatchers.Default)
            scope.launch {
                gotrue.exchangeCodeForSession(code)
                onSessionSuccess(gotrue.currentSessionOrNull() ?: error("No session available"))
            }
        }
    }
}
package io.github.jan.supabase.gotrue

import android.content.Intent
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.gotrue.providers.ExternalAuthConfigDefaults
import io.github.jan.supabase.gotrue.providers.OAuthProvider
import io.github.jan.supabase.gotrue.user.UserSession
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
/**
 * Handle deeplinks for authentication.
 * This handles the deeplinks for implicit and PKCE flow.
 * @param intent The intent from the activity
 * @param onSessionSuccess The callback when the session was successfully imported
 */
@OptIn(SupabaseExperimental::class)
fun SupabaseClient.handleDeeplinks(intent: Intent, onSessionSuccess: (UserSession) -> Unit = {}) {
    val data = intent.data ?: return
    val scheme = data.scheme ?: return
    val host = data.host ?: return
    if(scheme != gotrue.config.scheme || host != gotrue.config.host) return
    when(this.gotrue.config.flowType) {
        FlowType.IMPLICIT -> {
            val fragment = data.fragment ?: return
            gotrue.parseFragmentAndImportSession(fragment, onSessionSuccess)
        }
        FlowType.PKCE -> {
            val code = data.getQueryParameter("code") ?: return
            (gotrue as GoTrueImpl).authScope.launch {
                this@handleDeeplinks.gotrue.exchangeCodeForSession(code)
                onSessionSuccess(this@handleDeeplinks.gotrue.currentSessionOrNull() ?: error("No session available"))
            }
        }
    }
}
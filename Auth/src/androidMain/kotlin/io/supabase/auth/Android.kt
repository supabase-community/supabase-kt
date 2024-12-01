package io.supabase.auth

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import io.supabase.SupabaseClient
import io.supabase.annotations.SupabaseInternal
import io.supabase.auth.user.UserSession
import kotlinx.coroutines.launch

internal fun openUrl(uri: Uri, action: ExternalAuthAction) {
    when(action) {
        ExternalAuthAction.ExternalBrowser -> {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            applicationContext().startActivity(browserIntent)
        }
        is ExternalAuthAction.CustomTabs -> {
            val intent = CustomTabsIntent.Builder().apply(action.intentBuilder).build()
            intent.intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.launchUrl(applicationContext(), uri)
        }
    }
}

//TODO: Add context receiver 'Activity'
/**
 * Handle deeplinks for authentication.
 * This handles the deeplinks for implicit and PKCE flow.
 * @param intent The intent from the activity
 * @param onSessionSuccess The callback when the session was successfully imported
 */
@OptIn(SupabaseInternal::class)
fun SupabaseClient.handleDeeplinks(intent: Intent, onSessionSuccess: (UserSession) -> Unit = {}) {
    val data = intent.data ?: return
    val scheme = data.scheme ?: return
    val host = data.host ?: return
    if(scheme != auth.config.scheme || host != auth.config.host) return
    when(this.auth.config.flowType) {
        FlowType.IMPLICIT -> {
            val fragment = data.fragment ?: return
            auth.parseFragmentAndImportSession(fragment, onSessionSuccess)
        }
        FlowType.PKCE -> {
            val code = data.getQueryParameter("code") ?: return
            (auth as AuthImpl).authScope.launch {
                this@handleDeeplinks.auth.exchangeCodeForSession(code)
                onSessionSuccess(this@handleDeeplinks.auth.currentSessionOrNull() ?: error("No session available"))
            }
        }
    }
}
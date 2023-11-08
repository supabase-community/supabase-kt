package io.github.jan.supabase.gotrue

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.gotrue.ExternalAuthAction.CUSTOM_TABS
import io.github.jan.supabase.gotrue.ExternalAuthAction.EXTERNAL_BROWSER
import io.github.jan.supabase.gotrue.providers.ExternalAuthConfigDefaults
import io.github.jan.supabase.gotrue.providers.OAuthProvider
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.launch


internal fun Auth.openOAuth(provider: OAuthProvider, redirectTo: String, config: ExternalAuthConfigDefaults) {
    this as AuthImpl
    openUrl(
        uri = Uri.parse(oAuthUrl(provider, redirectTo) {
            scopes.addAll(config.scopes)
            queryParams.putAll(config.queryParams)
        }),
        action = this.config.defaultExternalAuthAction
    )
}

internal fun openUrl(uri: Uri, action: ExternalAuthAction) {
    when(action) {
        EXTERNAL_BROWSER -> {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri)
            browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            applicationContext().startActivity(browserIntent)
        }
        CUSTOM_TABS -> {
            val intent = CustomTabsIntent.Builder().build()
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
@OptIn(SupabaseExperimental::class)
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
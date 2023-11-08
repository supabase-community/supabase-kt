package io.github.jan.supabase.gotrue

import co.touchlab.kermit.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.launch
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem

/**
 * Handle deeplinks for authentication.
 * This handles the deeplinks for the implicit and the PKCE flow.
 * @param url The url from the ios app delegate
 * @param onSessionSuccess The callback when the session was successfully imported
 */
@SupabaseExperimental
fun SupabaseClient.handleDeeplinks(url: NSURL, onSessionSuccess: (UserSession) -> Unit = {}) {
    if (url.scheme != auth.config.scheme || url.host != auth.config.host) {
        Logger.d { "Received deeplink with wrong scheme or host" }
        return
    }
    when (auth.config.flowType) {
        FlowType.IMPLICIT -> {
            val fragment = url.fragment
            if (fragment == null) {
                Logger.d { "No fragment for deeplink" }
                return
            }
            auth.parseFragmentAndImportSession(fragment, onSessionSuccess)
        }
        FlowType.PKCE -> {
            val components = NSURLComponents(url, false)
            val code = (components.queryItems?.firstOrNull { it is NSURLQueryItem && it.name == "code" } as? NSURLQueryItem)?.value ?: return
            val scope = (auth as AuthImpl).authScope
            scope.launch {
                auth.exchangeCodeForSession(code)
                onSessionSuccess(auth.currentSessionOrNull() ?: error("No session available"))
            }
        }
    }
}
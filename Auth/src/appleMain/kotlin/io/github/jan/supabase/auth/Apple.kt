package io.github.jan.supabase.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.url.handledUrlParameterError
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.logging.d
import kotlinx.coroutines.launch
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem

/**
 * Handle deeplinks for authentication.
 * This handles the deeplinks for the implicit and the PKCE flow.
 * @param url The url from the ios app delegate
 * @param onSessionSuccess The callback when the session was successfully imported
 * @param onError Callback invoked if an error occurs during the [Auth.exchangeCodeForSession] call.
 */
fun SupabaseClient.handleDeeplinks(
    url: NSURL,
    onSessionSuccess: (UserSession) -> Unit = {},
    onError: (Throwable) -> Unit = {}
) {
    if (url.scheme != auth.config.scheme || url.host != auth.config.host) {
        Auth.logger.d { "Received deeplink with wrong scheme or host" }
        return
    }
    when (auth.config.flowType) {
        FlowType.IMPLICIT -> {
            val fragment = url.fragment
            if (fragment == null) {
                Auth.logger.d { "No fragment for deeplink" }
                return
            }
            auth.parseFragmentAndImportSession(fragment) {
                it?.let(onSessionSuccess)
            }
        }
        FlowType.PKCE -> {
            val components = NSURLComponents(url, false)
            if (auth.handledUrlParameterError{ key -> getQueryItem(components, key) }) {
                return
            }
            val code = getQueryItem(components, "code") ?: return
            val scope = (auth as AuthImpl).authScope
            scope.launch {
                try {
                    this@handleDeeplinks.auth.exchangeCodeForSession(code)
                    onSessionSuccess(this@handleDeeplinks.auth.currentSessionOrNull() ?: error("No session available"))
                } catch (e: Throwable) {
                    onError(e)
                }
            }
        }
    }
}

private fun getQueryItem(components: NSURLComponents, key: String): String? {
    return (components.queryItems?.firstOrNull { it is NSURLQueryItem && it.name == key } as? NSURLQueryItem)?.value
}
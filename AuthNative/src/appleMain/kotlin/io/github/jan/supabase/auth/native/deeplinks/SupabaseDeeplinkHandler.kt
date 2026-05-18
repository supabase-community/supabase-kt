package io.github.jan.supabase.auth.native.native.deeplinks

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.native.AuthFlowManager
import io.github.jan.supabase.auth.native.url.handledUrlParameterError
import io.github.jan.supabase.auth.parseFragmentAndImportSession
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.logging.d
import kotlinx.coroutines.launch
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem

/**
 * Handle deeplinks for authentication. Not necessary for OAuth.
 * @param url The URL from the iOS app delegate
 */
fun handleDeeplinks(url: NSURL) = AuthFlowManager.handleRedirect(url)

internal fun Auth.handleDeeplinks(
    url: NSURL,
    onSessionSuccess: (UserSession) -> Unit = {},
    onError: (Throwable) -> Unit = {}
) {
    /*if (url.scheme != config.scheme || url.host != config.host) { TODO: possibly ignore other
        logger.d { "Received deeplink with wrong scheme or host" }
        return
    }*/
    when (config.flowType) {
        FlowType.IMPLICIT -> {
            val fragment = url.fragment
            if (fragment == null) {
                logger.d { "No fragment for deeplink" }
                return
            }
            parseFragmentAndImportSession(fragment) {
                it?.let(onSessionSuccess)
            }
        }
        FlowType.PKCE -> {
            val components = NSURLComponents(url, false)
            if (handledUrlParameterError{ key -> getQueryItem(components, key) }) {
                return
            }
            val code = getQueryItem(components, "code") ?: return
            authScope.launch {
                try {
                    exchangeCodeForSession(code)
                    onSessionSuccess(currentSessionOrNull() ?: error("No session available"))
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
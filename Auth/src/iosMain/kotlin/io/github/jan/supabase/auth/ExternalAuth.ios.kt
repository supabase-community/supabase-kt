package io.github.jan.supabase.auth

import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASPresentationAnchor
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import kotlin.coroutines.resume

internal class PresentationContextProvider : NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {
    override fun presentationAnchorForWebAuthenticationSession(
        session: ASWebAuthenticationSession
    ): ASPresentationAnchor {
        return UIApplication.sharedApplication.keyWindow ?: ASPresentationAnchor()
    }
}

@Suppress("DEPRECATION")
internal actual suspend fun Auth.startExternalAuth(
    redirectUrl: String?,
    getUrl: suspend (redirectTo: String?) -> String,
    onSessionSuccess: suspend (UserSession) -> Unit
) {
    val url = getUrl(redirectUrl)
    val result = openIosAuthSession(
        getUrl(redirectUrl),
        config.scheme ?: return
    )
    handleDeeplinks(result)
}

internal suspend fun openIosAuthSession(
    url: String,
    scheme: String,
): NSURL {
    val nsUrl = NSURL.URLWithString(url) ?: error("Couldn't decode url $url")
    return suspendCancellableCoroutine {
        val provider = PresentationContextProvider()
        val session = ASWebAuthenticationSession(
            uRL = nsUrl,
            callbackURLScheme = scheme
        ) { callbackUrl, error ->
            if (error != null) {
                it.cancel(Exception(error.localizedDescription))
            } else if (callbackUrl != null) {
                it.resume(callbackUrl)
            }
        }

        session.presentationContextProvider = provider
        session.prefersEphemeralWebBrowserSession = false
        session.start()
    }
}
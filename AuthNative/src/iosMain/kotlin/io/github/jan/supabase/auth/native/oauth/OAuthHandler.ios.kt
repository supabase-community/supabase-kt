package io.github.jan.supabase.auth.native.native.oauth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.native.deeplinks.handleDeeplinks
import io.github.jan.supabase.auth.native.platformConfig
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.logging.d
import io.github.jan.supabase.logging.e
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

internal actual suspend fun Auth.startOAuthSession(
    redirectUrl: String?,
    getUrl: suspend (redirectTo: String?) -> String,
    onSessionSuccess: suspend (UserSession) -> Unit
) {
    val url = getUrl(redirectUrl)
    val result = openIosAuthSession(
        getUrl(redirectUrl),
        config.platformConfig()?.appScheme ?: error("No app scheme provided in the config or Auth Native not initialized")
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

internal actual suspend fun SupabaseClient.openExternalUrl(url: String) {
    UIApplication.sharedApplication.openURL(NSURL(string = url), emptyMap<Any?, Any>()) {
        if(it) logger.d { "Successfully opened provider url in safari" } else logger.e { "Failed to open provider url in safari" }
    }
}
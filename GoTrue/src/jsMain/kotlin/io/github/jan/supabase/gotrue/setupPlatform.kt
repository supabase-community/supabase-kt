package io.github.jan.supabase.gotrue

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.gotrue.user.UserSession
import io.ktor.util.PlatformUtils.IS_BROWSER
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.w3c.dom.url.URL

@SupabaseInternal
actual fun Auth.setupPlatform() {
    this as AuthImpl

    fun checkForHash() {
        if(window.location.hash.isBlank()) return

        val afterHash = window.location.hash.substring(1)

        if(!afterHash.contains('=')) {
            // No params after hash, no need to continue
            return
        }

        val map = afterHash.split("&").associate {
            it.split("=").let { pair ->
                pair[0] to pair[1]
            }
        }
        val accessToken = map["access_token"] ?: return
        val refreshToken = map["refresh_token"] ?: return
        val expiresIn = map["expires_in"]?.toLong() ?: return
        val tokenType = map["token_type"] ?: return
        val type = map["type"]
        val providerToken = map["provider_token"]
        val providerRefreshToken = map["provider_refresh_token"]
        val scope = CoroutineScope(config.coroutineDispatcher)
        scope.launch {
            val user = retrieveUser(accessToken)
            importSession(UserSession(accessToken, refreshToken, providerRefreshToken, providerToken, expiresIn, tokenType, user, type ?: ""))
        }
        val newURL = window.location.href.split("?")[0];
        window.history.replaceState({}, window.document.title, newURL);
    }

    fun checkForPCKECode() {
        val url = URL(window.location.href)
        val code = url.searchParams.get("code") ?: return
        val scope = CoroutineScope(config.coroutineDispatcher)
        scope.launch {
            val session = exchangeCodeForSession(code)
            importSession(session)
        }
        val newURL = window.location.href.split("?")[0];
        window.history.replaceState({}, window.document.title, newURL);
    }

    if(IS_BROWSER) {
        window.onhashchange = {
            checkForHash()
        }
        window.onload = {
            checkForHash()
            checkForPCKECode()
        }
    }
}
package io.github.jan.supabase.gotrue

import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.w3c.dom.url.URL

actual fun GoTrue.setupPlatform() {
    this as GoTrueImpl

    fun checkForHash() {
        if(window.location.hash.isBlank()) return
        val map = window.location.hash.substring(1).split("&").associate {
            it.split("=").let { pair ->
                pair[0] to pair[1]
            }
        }
        val accessToken = map["access_token"]
        val refreshToken = map["refresh_token"]
        val expiresIn = map["expires_in"]?.toLong()
        val tokenType = map["token_type"]
        val type = map["type"]
        val providerToken = map["provider_token"]
        val providerRefreshToken = map["provider_refresh_token"]
        val scope = CoroutineScope(config.coroutineDispatcher)
        if(accessToken != null && refreshToken != null && expiresIn != null && tokenType != null) {
            scope.launch {
                val user = retrieveUser(accessToken)
                startAutoRefresh(UserSession(accessToken, refreshToken, providerRefreshToken, providerToken, expiresIn, tokenType, user, type ?: ""))
            }
        }
    }

    fun checkForPCKECode() {
        val url = URL(window.location.href)
        val code = url.searchParams.get("code") ?: return
        val scope = CoroutineScope(config.coroutineDispatcher)
        scope.launch {
            val session = exchangeCodeForSession(code)
            startAutoRefresh(session)
        }
    }

    window.onhashchange = {
        checkForHash()
    }
    window.onload = {
        checkForHash()
        checkForPCKECode()
    }
}
package io.github.jan.supabase.gotrue

import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        val scope = CoroutineScope(config.coroutineDispatcher)
        if(accessToken != null && refreshToken != null && expiresIn != null && tokenType != null) {
            scope.launch {
                val user = getUser(accessToken)
                startAutoRefresh(UserSession(accessToken, refreshToken, expiresIn, tokenType, user, type ?: ""))
            }
        }
    }

    window.onhashchange = {
        checkForHash()
    }
    window.onload = {
        checkForHash()
    }
}
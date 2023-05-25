package io.github.jan.supabase.gotrue

import io.github.jan.supabase.annotiations.SupabaseInternal
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.w3c.dom.url.URL

@SupabaseInternal
actual fun GoTrue.setupPlatform() {
    this as GoTrueImpl

    fun checkForHash() {
        if(window.location.hash.isBlank()) return
        val map = window.location.hash.substring(1).split("&").associate {
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
    }

    fun checkForPCKECode() {
        val url = URL(window.location.href)
        val code = url.searchParams.get("code") ?: return
        val scope = CoroutineScope(config.coroutineDispatcher)
        scope.launch {
            val session = exchangeCodeForSession(code)
            importSession(session)
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
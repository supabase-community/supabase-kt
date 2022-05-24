package io.github.jan.supacompose.auth

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.user.UserSession
import io.github.jan.supacompose.plugins.SupabasePlugin
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class Web {

    class Config

    companion object : SupabasePlugin<Config, Web> {

        override val key = "web"

        override fun create(supabaseClient: SupabaseClient, config: Config.() -> Unit): Web {
            val authPlugin = supabaseClient.plugins["auth"] as? AuthImpl ?: throw IllegalStateException("Auth plugin is not installed")
            window.addEventListener("hashchange", {
                val map = window.location.hash.split("&").associate {
                    it.split("=").let { pair ->
                        pair[0] to pair[1]
                    }
                }
                val accessToken = map["access_token"] ?: return@addEventListener
                val refreshToken = map["refresh_token"] ?: return@addEventListener
                val expiresIn = map["expires_in"]?.toLong() ?: return@addEventListener
                val tokenType = map["token_type"] ?: return@addEventListener
                val scope = CoroutineScope(Dispatchers.Default)
                scope.launch {
                    val user = authPlugin.goTrueClient.getUser(accessToken)
                    authPlugin.startJob(UserSession(accessToken, refreshToken, expiresIn, tokenType, user))
                }
            })
            return Web()
        }

    }

}
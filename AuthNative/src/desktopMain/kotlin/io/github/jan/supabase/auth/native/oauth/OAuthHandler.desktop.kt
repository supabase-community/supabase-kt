package io.github.jan.supabase.auth.native.native.oauth

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.native.oauth.server.createServer
import io.github.jan.supabase.auth.native.platformConfig
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal actual suspend fun Auth.startOAuthSession(
    redirectUrl: String?,
    getUrl: suspend (redirectTo: String?) -> String,
    onSessionSuccess: suspend (UserSession) -> Unit
) {
    withContext(Dispatchers.IO) {
        if(redirectUrl != null) {
            config.platformConfig()?.urlLauncher?.openUrl(supabaseClient, getUrl(redirectUrl)) ?: error("Auth Native not initialized")
            return@withContext
        }
        createServer({
            getUrl(it)
        }, supabaseClient.auth, onSessionSuccess)
    }
}
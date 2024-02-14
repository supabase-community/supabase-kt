package io.github.jan.supabase.gotrue

import io.github.jan.supabase.gotrue.server.createServer
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal actual suspend fun Auth.startExternalAuth(
    redirectUrl: String?,
    getUrl: suspend (redirectTo: String?) -> String,
    onSessionSuccess: suspend (UserSession) -> Unit
) {
    withContext(Dispatchers.IO) {
        if(redirectUrl != null) {
            supabaseClient.openExternalUrl(getUrl(redirectUrl))
            return@withContext
        }
        launch {
            createServer({
                getUrl(it)
            }, supabaseClient.auth, onSessionSuccess)
        }
    }
}
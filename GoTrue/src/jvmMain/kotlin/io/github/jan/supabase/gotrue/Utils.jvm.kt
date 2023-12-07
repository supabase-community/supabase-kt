package io.github.jan.supabase.gotrue

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.providers.createServer
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.net.URI

internal actual suspend fun SupabaseClient.openExternalUrl(url: String) {
    withContext(Dispatchers.IO) {
        Desktop.getDesktop()
            .browse(URI(url))
    }
}

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
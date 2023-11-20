package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.createServer
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal actual suspend fun SSO.loginWithSSO(
    supabaseClient: SupabaseClient,
    onSuccess: suspend (UserSession) -> Unit,
    redirectUrl: String?,
    config: (SSO.Config.() -> Unit)?
) {
    withContext(Dispatchers.IO) {
        launch {
            createServer({
                val result = supabaseClient.auth.retrieveSSOUrl(redirectUrl ?: it) { config?.invoke(this) }
                result.url
            }, supabaseClient.auth, onSuccess)
        }
    }
}
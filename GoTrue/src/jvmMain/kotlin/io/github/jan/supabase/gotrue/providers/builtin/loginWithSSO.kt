package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.createServer
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal actual suspend fun <Config : SSO.Config> SSO<Config>.loginWithSSO(
    supabaseClient: SupabaseClient,
    onSuccess: suspend (UserSession) -> Unit,
    redirectUrl: String?,
    config: (Config.() -> Unit)?
) {
    withContext(Dispatchers.IO) {
        launch {
            createServer({
                val result = supabaseClient.auth.retrieveSSOUrl(this@loginWithSSO, redirectUrl ?: it, config)
                result.url
            }, supabaseClient.auth, onSuccess)
        }
    }
}
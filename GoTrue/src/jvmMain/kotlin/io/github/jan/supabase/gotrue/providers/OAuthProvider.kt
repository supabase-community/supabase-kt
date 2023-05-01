package io.github.jan.supabase.gotrue.providers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.net.URI

actual abstract class OAuthProvider : AuthProvider<ExternalAuthConfig, Unit> {

    actual abstract val name: String

    actual override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        withContext(Dispatchers.IO) {
            if(redirectUrl != null) {
                Desktop.getDesktop()
                    .browse(URI(supabaseClient.gotrue.resolveUrl("authorize?provider=$name&redirect_to=$redirectUrl")))
                return@withContext
            }
            launch {
                createServer({ supabaseClient.gotrue.resolveUrl("authorize?provider=$name&redirect_to=$it") }, supabaseClient.gotrue, onSuccess)
            }
        }
    }

    actual override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) = login(supabaseClient, onSuccess, redirectUrl, config = config)

    actual companion object


}
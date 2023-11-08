package io.github.jan.supabase.gotrue.providers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.net.URI

/**
 * Represents an OAuth provider.
 */
actual abstract class OAuthProvider : AuthProvider<ExternalAuthConfig, Unit> {

    /**
     * The name of the provider.
     */
    actual abstract val name: String

    actual override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (ExternalAuthConfig.() -> Unit)?
    ) {
        val externalConfig = ExternalAuthConfig().apply { config?.invoke(this) }
        withContext(Dispatchers.IO) {
            if(redirectUrl != null) {
                Desktop.getDesktop()
                    .browse(URI(supabaseClient.auth.oAuthUrl(this@OAuthProvider, redirectUrl) {
                        scopes.addAll(externalConfig.scopes)
                        queryParams.putAll(externalConfig.queryParams)
                    }))
                return@withContext
            }
            launch {
                createServer({
                    supabaseClient.auth.oAuthUrl(this@OAuthProvider, it) {
                        scopes.addAll(externalConfig.scopes)
                        queryParams.putAll(externalConfig.queryParams)
                    }
                }, supabaseClient.auth, onSuccess)
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
package io.github.temk0.supabase.authui

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.plugins.MainConfig
import io.github.jan.supabase.plugins.MainPlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.ktor.client.statement.HttpResponse



sealed interface AuthUI : MainPlugin<AuthUI.Config> {

    data class Config(
        override var customUrl: String? = null,
        override var jwtToken: String? = null,
        var idTokenRequest: GoogleIDTokenRequest? = null
    ) : MainConfig {

        data class GoogleIDTokenRequest(
            val isSupported: Boolean = true,
            val filterByAuthorizedAccounts: Boolean = false,
            val serverCliendId: String = "",
            val associateLinkedAccounts: Pair<String, List<String>>? = null,
            val nonce: String? = null
        )
    }

    companion object : SupabasePluginProvider<Config, AuthUI> {

        override val key: String = "authui"

        const val API_VERSION = 1

        override fun create(supabaseClient: SupabaseClient, config: Config): AuthUI {
            return AuthUIImpl(config, supabaseClient)
        }

        override fun createConfig(init: Config.() -> Unit): Config {
            return Config().apply(init)
        }
    }

    suspend fun loginWithGoogle(idToken: String)
}

class AuthUIImpl(
    override val config: AuthUI.Config,
    override val supabaseClient: SupabaseClient,
) : AuthUI {

    override val apiVersion = AuthUI.API_VERSION

    override val pluginKey = AuthUI.key

    override suspend fun parseErrorResponse(response: HttpResponse): RestException {
        return supabaseClient.gotrue.parseErrorResponse(response)
    }

    override suspend fun loginWithGoogle(idToken: String) {
        supabaseClient.gotrue.loginWith(IDToken) {
            provider = Google
            this.idToken = idToken
        }
    }
}


val SupabaseClient.authUI: AuthUI
    get() = pluginManager.getPlugin(AuthUI)



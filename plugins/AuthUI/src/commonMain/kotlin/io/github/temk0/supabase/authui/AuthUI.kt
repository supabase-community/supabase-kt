package io.github.temk0.supabase.authui

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.plugins.MainConfig
import io.github.jan.supabase.plugins.MainPlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.ktor.client.statement.HttpResponse


sealed interface AuthUI : MainPlugin<AuthUI.Config> {

    data class Config(
        override var customUrl: String? = null,
        override var jwtToken: String? = null,
        var idTokenRequest: GoogleIDTokenRequest.() -> Unit = {}
    ) : MainConfig {

        data class GoogleIDTokenRequest(
            val isSupported: Boolean = true,
            val filterByAuthorizedAccounts: Boolean = false,
            val serverCliendId: String? = null,
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
}


val SupabaseClient.authUI: AuthUI
    get() = pluginManager.getPlugin(AuthUI)



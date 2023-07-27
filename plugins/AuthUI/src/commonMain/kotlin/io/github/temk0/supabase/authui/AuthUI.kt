package io.github.temk0.supabase.authui

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider


sealed interface AuthUI : SupabasePlugin {

    data class Config(
        var loginConfig: LoginConfig? = null
    ) : SupabasePlugin

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
    val config: AuthUI.Config,
    val supabaseClient: SupabaseClient,
) : AuthUI {

    val apiVersion = AuthUI.API_VERSION

    val pluginKey = AuthUI.key

    override suspend fun loginWithGoogle(idToken: String) {
        supabaseClient.gotrue.loginWith(IDToken) {
            provider = Google
            this.idToken = idToken
        }
    }
}


val SupabaseClient.authUI: AuthUI
    get() = pluginManager.getPlugin(AuthUI)



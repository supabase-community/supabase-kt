package io.github.jan.supabase.compose.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider


sealed interface ComposeAuth : SupabasePlugin {

    data class Config(
        var loginConfig: LoginConfig? = null
    ) : SupabasePlugin

    companion object : SupabasePluginProvider<Config, ComposeAuth> {

        override val key: String = "composeauth"

        const val API_VERSION = 1

        override fun create(supabaseClient: SupabaseClient, config: Config): ComposeAuth {
            return ComposeAuthImpl(config, supabaseClient)
        }

        override fun createConfig(init: Config.() -> Unit): Config {
            return Config().apply(init)
        }
    }

    suspend fun fallbackLogin()
}

class ComposeAuthImpl(
    val config: ComposeAuth.Config,
    val supabaseClient: SupabaseClient,
) : ComposeAuth {

    val apiVersion = ComposeAuth.API_VERSION

    val pluginKey = ComposeAuth.key

    suspend fun loginWithGoogle(idToken: String) {
        val nonce = if(config.loginConfig == null || config.loginConfig !is GoogleLoginConfig) null else (config.loginConfig as GoogleLoginConfig).nonce

        supabaseClient.gotrue.loginWith(IDToken) {
            provider = Google
            this.idToken = idToken
            this.nonce = nonce
        }
    }

    override suspend fun fallbackLogin() {
        supabaseClient.gotrue.loginWith(Google)
    }
}


val SupabaseClient.composeAuth: ComposeAuth
    get() = pluginManager.getPlugin(ComposeAuth)



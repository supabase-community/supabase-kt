package io.github.jan.supabase.compose.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.LogoutScope
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
}

class ComposeAuthImpl(
    val config: ComposeAuth.Config,
    val supabaseClient: SupabaseClient,
) : ComposeAuth {

    val apiVersion = ComposeAuth.API_VERSION

    val pluginKey = ComposeAuth.key

}

internal suspend fun ComposeAuth.loginWithGoogle(idToken: String) {
    this as ComposeAuthImpl
    val nonce = (config.loginConfig as? GoogleLoginConfig)?.nonce

    supabaseClient.gotrue.loginWith(IDToken) {
        provider = Google
        this.idToken = idToken
        this.nonce = nonce
    }
}

internal suspend fun ComposeAuth.fallbackLogin() {
    this as ComposeAuthImpl
    supabaseClient.gotrue.loginWith(Google)
}

internal suspend fun ComposeAuth.signOut(scope: LogoutScope = LogoutScope.LOCAL) {
    this as ComposeAuthImpl
    supabaseClient.gotrue.logout(scope)
}


val SupabaseClient.composeAuth: ComposeAuth
    get() = pluginManager.getPlugin(ComposeAuth)



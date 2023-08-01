package io.github.jan.supabase.compose.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.LogoutScope
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.Apple
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.IDTokenProvider
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider


sealed interface ComposeAuth : SupabasePlugin {

    val config: Config

    val supabaseClient: SupabaseClient

    data class Config(
        val googleLoginConfig: GoogleLoginConfig? = null,
        val appleLoginConfig: AppleLoginConfig? = null
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

internal class ComposeAuthImpl(
    override val config: ComposeAuth.Config,
    override val supabaseClient: SupabaseClient,
) : ComposeAuth {

    val apiVersion = ComposeAuth.API_VERSION

    val pluginKey = ComposeAuth.key

}

internal suspend fun ComposeAuth.loginWithGoogle(idToken: String) {
    supabaseClient.gotrue.loginWith(IDToken) {
        provider = Google
        this.idToken = idToken
        nonce = config.googleLoginConfig?.nonce
        data = config.googleLoginConfig?.extraData
    }
}

internal suspend fun ComposeAuth.loginWithApple(idToken: String){
    supabaseClient.gotrue.loginWith(IDToken){
        provider = Apple
        this.idToken = idToken
        nonce = config.appleLoginConfig?.nonce
        data = config.appleLoginConfig?.extraData
    }
}

internal suspend fun ComposeAuth.fallbackLogin(provider: IDTokenProvider) {
    supabaseClient.gotrue.loginWith(provider)
}

internal suspend fun ComposeAuth.signOut(scope: LogoutScope = LogoutScope.LOCAL) {
    supabaseClient.gotrue.logout(scope)
}


val SupabaseClient.composeAuth: ComposeAuth
    get() = pluginManager.getPlugin(ComposeAuth)

package io.github.jan.supabase.compose.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.LogoutScope
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.github.jan.supabase.toJsonObject
import kotlinx.serialization.json.buildJsonObject


sealed interface ComposeAuth : SupabasePlugin {

    val config: Config

    val supabaseClient: SupabaseClient

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
    override val config: ComposeAuth.Config,
    override val supabaseClient: SupabaseClient,
) : ComposeAuth {

    val apiVersion = ComposeAuth.API_VERSION

    val pluginKey = ComposeAuth.key

}

internal suspend fun ComposeAuth.loginWithGoogle(idToken: String, extraData:List<LoginConfig.ExtraData> = emptyList()) {
    val nonce = (config.loginConfig as? GoogleLoginConfig)?.nonce

    supabaseClient.gotrue.loginWith(IDToken) {
        provider = Google
        this.idToken = idToken
        this.nonce = nonce
        data = buildJsonObject{
            extraData.forEach {
                it.value?.toJsonObject()?.let { it1 -> this.put(it.key, it1) }
            }
        }
    }
}

internal suspend fun ComposeAuth.fallbackLogin() {
    supabaseClient.gotrue.loginWith(Google)
}

internal suspend fun ComposeAuth.signOut(scope: LogoutScope = LogoutScope.LOCAL) {
    supabaseClient.gotrue.logout(scope)
}


val SupabaseClient.composeAuth: ComposeAuth
    get() = pluginManager.getPlugin(ComposeAuth)



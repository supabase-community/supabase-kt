package io.github.jan.supabase.compose.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.LogoutScope
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.Apple
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.IDTokenProvider
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider

/**
 * Plugin that extends [GoTrue] Module with composable function that enables
 * easy implementation of Native Login.
 * Currently supported Google Login (Android) and Apple Login (iOS), other compose-supported targets rely on GoTrue login.
 *
 * To use it, install GoTrue and ComposeAuth
 * ```kotlin
 * val client = createSupabaseClient(supabaseUrl, supabaseKey) {
 *    install(GoTrue) {
 *       //your config here
 *    }
 *    install(ComposeAuth) {
 *       googleNativeLogin(/* your config parameters here */)
 *       appleNativeLogin(/* your config parameters here */)
 *    }
 * }
 * ```
 *
 * then on you screen call
 *  ```kotlin
 *  val action = auth.rememberLoginWithGoogle(
 *     onResult = {
 *        // returns NativeSignInResult
 *     },
 *     fallback = {
 *        // optional: only add fallback if you like to use custom fallback
 *     }
 * )
 *
 * Button(
 *     onClick = {
 *         action.startFlow()
 *     }
 * ) {
 *     Text(text = "Google Login")
 * }
 *  ```
 */
sealed interface ComposeAuth : SupabasePlugin {

    /**
     * Returns native login configurations
     */
    val config: Config

    /**
     * The corresponding [SupabaseClient] instance
     */
    val supabaseClient: SupabaseClient

    /**
     * Config for [ComposeAuth]
     * @param loginConfig provide [LoginConfig]
     *
     */
    data class Config(
        val loginConfig: MutableMap<String, LoginConfig> = mutableMapOf()
    ) : SupabasePlugin

    companion object : SupabasePluginProvider<Config, ComposeAuth> {

        override val key: String = "composeauth"

        override fun create(supabaseClient: SupabaseClient, config: Config): ComposeAuth {
            return ComposeAuthImpl(config, supabaseClient)
        }

        override fun createConfig(init: Config.() -> Unit): Config {
            return Config().apply(init)
        }
    }
}

/**
 * Composable plugin that handles native login using GoTrue
 */
val SupabaseClient.composeAuth: ComposeAuth
    get() = pluginManager.getPlugin(ComposeAuth)

internal class ComposeAuthImpl(
    override val config: ComposeAuth.Config,
    override val supabaseClient: SupabaseClient,
) : ComposeAuth

internal suspend fun ComposeAuth.loginWithGoogle(idToken: String) {
    val config = config.loginConfig["google"] as? GoogleLoginConfig

    supabaseClient.auth.signInWith(IDToken) {
        provider = Google
        this.idToken = idToken
        nonce = config?.nonce
        data = config?.extraData
    }
}

internal suspend fun ComposeAuth.loginWithApple(idToken: String) {
    val config = config.loginConfig["apple"] as? GoogleLoginConfig

    supabaseClient.auth.signInWith(IDToken) {
        provider = Apple
        this.idToken = idToken
        nonce = config?.nonce
        data = config?.extraData
    }
}

internal suspend fun ComposeAuth.fallbackLogin(provider: IDTokenProvider) {
    supabaseClient.auth.signInWith(provider)
}

internal suspend fun ComposeAuth.signOut(scope: LogoutScope = LogoutScope.LOCAL) {
    supabaseClient.auth.logout(scope)
}
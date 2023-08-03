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

/**
 * Plugin that extends [GoTrue] Module with composable function that enables
 * easy implementation of Native Login.
 * Currently supported Google Login(Android) and Apple Login(iOS)
 *
 * To use it install GoTrue and ComposeAuth
 * ```kotlin
 * val client = createSupabaseClient(supabaseUrl, supabaseKey) {
 *    install(GoTrue){
 *      //your config here
 *    }
 *    install(ComposeAuth){
 *       googleLoginConfig = googleNativeLogin(/* your config parameters here */),
 *       appleLoginConfig = appleNativeLogin(/* your config parameters here */)
 *    }
 * }
 * ```
 *
 * then on you screen call
 *  ```kotlin
 *  val action = auth.rememberLoginWithGoogle(
 *         onResult = {
 *          // returns NativeSignInResult
 *         },
 *         fallback = {
 *          // optional: only add fallback if you like to use custom fallback
 *         }
 *
 * Button(onClick = {
 *      action.startFlow()
 * })
 * {
 *      Text(text = "Google Login")
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
     */
    data class Config(
        val googleLoginConfig: GoogleLoginConfig? = null,
        val appleLoginConfig: AppleLoginConfig? = null
    ) : SupabasePlugin

    companion object : SupabasePluginProvider<Config, ComposeAuth> {

        override val key: String = "composeauth"

        /**
         * The version for the api the plugin is using
         */
        const val API_VERSION = 1

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

internal suspend fun ComposeAuth.loginWithApple(idToken: String) {
    supabaseClient.gotrue.loginWith(IDToken) {
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


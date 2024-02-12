package io.github.jan.supabase.compose.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.SignOutScope
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Apple
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.IDTokenProvider
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.logging.SupabaseLogger
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider

/**
 * Plugin that extends the [Auth] Module with composable function that enables an easy implementation of Native Auth.
 * Currently supported Google Login (Android with OneTap or CM on Android 14+) and Apple Login (iOS), other compose-supported targets rely on GoTrue login.
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
 *  val action = auth.rememberSignInWithGoogle(
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
sealed interface ComposeAuth : SupabasePlugin<ComposeAuth.Config> {

    /**
     * Config for [ComposeAuth]
     * @param loginConfig provide [LoginConfig]
     *
     */
    data class Config(
        val loginConfig: MutableMap<String, LoginConfig> = mutableMapOf()
    )

    companion object : SupabasePluginProvider<Config, ComposeAuth> {

        override val KEY: String = "composeauth"

        override val LOGGER: SupabaseLogger = SupabaseClient.createLogger("Supabase-ComposeAuth")

        override fun create(supabaseClient: SupabaseClient, config: Config): ComposeAuth {
            return ComposeAuthImpl(config, supabaseClient)
        }

        override fun createConfig(init: Config.() -> Unit): Config {
            return Config().apply(init)
        }
    }
}

/**
 * Composable plugin that handles Native Auth on supported platforms
 */
val SupabaseClient.composeAuth: ComposeAuth
    get() = pluginManager.getPlugin(ComposeAuth)

internal class ComposeAuthImpl(
    override val config: ComposeAuth.Config,
    override val supabaseClient: SupabaseClient,
) : ComposeAuth

internal suspend fun ComposeAuth.signInWithGoogle(idToken: String) {
    val config = config.loginConfig["google"] as? GoogleLoginConfig

    supabaseClient.auth.signInWith(IDToken) {
        provider = Google
        this.idToken = idToken
        nonce = config?.nonce
        data = config?.extraData
    }
}

internal suspend fun ComposeAuth.signInWithApple(idToken: String) {
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

internal suspend fun ComposeAuth.signOut(scope: SignOutScope = SignOutScope.LOCAL) {
    supabaseClient.auth.signOut(scope)
}
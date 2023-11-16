package io.github.jan.supabase.gotrue

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.plugins.SupabasePluginProvider

/**
 * Plugin to interact with the supabase Auth API
 *
 * To use it you need to install it to the [SupabaseClient]:
 * ```kotlin
 * val supabase = createSupabaseClient(supabaseUrl, supabaseKey) {
 *    install(GoTrue)
 * }
 * ```
 *
 * then you can use it like this:
 * ```kotlin
 * val result = supabase.gotrue.signUpWith(Email) {
 *   email = "example@email.com"
 *   password = "password"
 * }
 * ```
 */
@Deprecated("Use the Auth plugin instead", ReplaceWith("Auth", "io.github.jan.supabase.gotrue.Auth"), DeprecationLevel.WARNING)
interface GoTrue : Auth {

    @Deprecated("Use the Auth plugin instead", ReplaceWith("Auth", "io.github.jan.supabase.gotrue.Auth"), DeprecationLevel.WARNING)
    companion object : SupabasePluginProvider<AuthConfig, GoTrue> {

        override val key = "auth"

        /**
         * The gotrue api version to use
         */
        const val API_VERSION = 1

        override fun createConfig(init: AuthConfig.() -> Unit) = AuthConfig().apply(init)
        override fun create(supabaseClient: SupabaseClient, config: AuthConfig): GoTrue = GoTrueImpl(AuthImpl(supabaseClient, config))

    }

}

internal class GoTrueImpl(auth: Auth): GoTrue, Auth by auth
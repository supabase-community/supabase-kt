package io.github.jan.supabase.gotrue

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.plugins.SupabasePluginProvider

/**
 * Plugin to interact with the supabase Auth API
 *
 * To use it you need to install it to the [SupabaseClient]:
 * ```kotlin
 * val client = createSupabaseClient(supabaseUrl, supabaseKey) {
 *    install(GoTrue)
 * }
 * ```
 *
 * then you can use it like this:
 * ```kotlin
 * val result = client.gotrue.signUpWith(Email) {
 *   email = "example@email.com"
 *   password = "password"
 * }
 * ```
 */
interface GoTrue : Auth {

    companion object : SupabasePluginProvider<AuthConfig, Auth> {

        override val key = "auth"

        /**
         * The gotrue api version to use
         */
        const val API_VERSION = 1

        override fun createConfig(init: AuthConfig.() -> Unit) = AuthConfig().apply(init)
        override fun create(supabaseClient: SupabaseClient, config: AuthConfig): Auth = AuthImpl(supabaseClient, config)

    }

}
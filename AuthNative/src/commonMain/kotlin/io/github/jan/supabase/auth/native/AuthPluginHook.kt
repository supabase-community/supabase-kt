package io.github.jan.supabase.auth.native.native

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.plugins.SupabasePluginProvider

data object AuthPluginHook : SupabasePluginProvider<AuthConfig, Auth> {
    override val key: String = "auth"

    override fun createConfig(init: AuthConfig.() -> Unit) = AuthConfig().apply {
        initializeNativeAuth()
        init()
    }

    override fun create(supabaseClient: SupabaseClient, config: AuthConfig): Auth = Auth.create(supabaseClient, config)

}

fun Auth.Companion.withNative() = AuthPluginHook
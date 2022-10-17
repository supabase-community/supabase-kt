package io.github.jan.supabase.plugins

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder

interface SupabasePlugin {

    suspend fun close() {}

}

interface SupabasePluginProvider<Config, PluginInstance : SupabasePlugin> {

    val key: String
    fun createConfig(init: Config.() -> Unit): Config
    fun setup(builder: SupabaseClientBuilder, config: Config) {}
    fun create(supabaseClient: SupabaseClient, config: Config) : PluginInstance

}
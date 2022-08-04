package io.github.jan.supacompose.plugins

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.SupabaseClientBuilder

interface SupacomposePlugin {

    suspend fun close() {}

}

interface SupacomposePluginProvider<Config, PluginInstance : SupacomposePlugin> {

    val key: String
    fun createConfig(init: Config.() -> Unit): Config
    fun setup(builder: SupabaseClientBuilder, config: Config) {}
    fun create(supabaseClient: SupabaseClient, config: Config) : PluginInstance

}
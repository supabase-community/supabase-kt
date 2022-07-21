package io.github.jan.supacompose.plugins

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.SupabaseClientBuilder

interface SupacomposePlugin<Config, PluginInstance> {

    val key: String

    fun createConfig(init: Config.() -> Unit): Config
    fun setup(builder: SupabaseClientBuilder, config: Config) {}
    fun create(supabaseClient: SupabaseClient, config: Config) : PluginInstance

}
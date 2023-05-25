package io.github.jan.supabase.plugins

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder

/**
 * A plugin is a feature that can be installed into the supabase client
 */
interface SupabasePlugin {

    /**
     * Free all resources used by this plugin
     */
    suspend fun close() {}

}

/**
 * A plugin provider is used to create a plugin instance. Typically inherited by a companion object of the plugin
 */
interface SupabasePluginProvider<Config, PluginInstance : SupabasePlugin> {

    /**
     * The key of this plugin. This key is used to identify the plugin within the [PluginManager]
     */
    val key: String

    /**
     * Create a config for this plugin using the [init] function
     */
    fun createConfig(init: Config.() -> Unit): Config

    /**
     * Change the [SupabaseClientBuilder] using the [config]
     */
    fun setup(builder: SupabaseClientBuilder, config: Config) {}

    /**
     * Create an instance of this plugin using the [supabaseClient] and [config]
     */
    fun create(supabaseClient: SupabaseClient, config: Config) : PluginInstance

}
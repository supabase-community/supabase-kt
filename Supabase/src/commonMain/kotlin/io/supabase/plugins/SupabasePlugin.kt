package io.supabase.plugins

import io.supabase.SupabaseClient
import io.supabase.SupabaseClientBuilder
import io.supabase.logging.LogLevel
import io.supabase.logging.SupabaseLogger

/**
 * A plugin is a feature that can be installed into the supabase client
 */
interface SupabasePlugin<Config> {

    /**
     * The config for this plugin
     */
    val config: Config

    /**
     * The corresponding [SupabaseClient]
     */
    val supabaseClient: SupabaseClient

    /**
     * Free all resources used by this plugin
     */
    suspend fun close() {}

    /**
     * Initialize the plugin. Use this function, if you want to execute code after the plugin has been created but also have to access other plugins.
     *
     * **Note:** This function is called by the [SupabaseClient] after all plugins have been created. Do not call this function manually.
     */
    fun init() {}

}

/**
 * A plugin provider is used to create a plugin instance. Typically inherited by a companion object of the plugin
 */
interface SupabasePluginProvider<Config, PluginInstance : SupabasePlugin<Config>> {

    /**
     * The key of this plugin. This key is used to identify the plugin within the [PluginManager]
     */
    val key: String

    /**
     * The logger used in this plugin.
     */
    val logger: SupabaseLogger

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

    /**
     * Updates the plugin's log level
     */
    fun setLogLevel(level: LogLevel) {
        logger.setLevel(level)
    }

}

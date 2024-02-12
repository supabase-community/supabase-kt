package io.github.jan.supabase.plugins

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.logging.KermitSupabaseLogger
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.logging.SupabaseLogger

open class SupabasePluginConfig {
    var logLevel: LogLevel? = null
    var logger: (level: LogLevel, tag: String) -> SupabaseLogger = { level, tag -> KermitSupabaseLogger(level, tag) }
}

/**
 * A plugin is a feature that can be installed into the supabase client
 */
interface SupabasePlugin<Config : SupabasePluginConfig> {

    val config: Config
    val supabaseClient: SupabaseClient

    /**
     * Free all resources used by this plugin
     */
    open suspend fun close() {}

}

/**
 * A plugin provider is used to create a plugin instance. Typically inherited by a companion object of the plugin
 */
interface SupabasePluginProvider<Config : SupabasePluginConfig, PluginInstance : SupabasePlugin<Config>> {

    /**
     * The key of this plugin. This key is used to identify the plugin within the [PluginManager]
     */
    val KEY: String

    /**
     * The logger used in this plugin.
     */
    val LOGGER: SupabaseLogger

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
     * Updates the plugin's logging level
     */
    fun setLoggingLevel(level: LogLevel) {
        LOGGER.setLevel(level)
    }

}
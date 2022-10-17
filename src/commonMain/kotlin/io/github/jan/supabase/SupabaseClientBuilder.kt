package io.github.jan.supabase

import io.github.jan.supabase.annotiations.SupaComposeDsl
import io.github.jan.supabase.plugins.PluginManager
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine

/**
 * Creates a new [SupabaseClient] with the given options.
 *
 * Use [createSupabaseClient] to create a new instance of [SupabaseClient].
 */
@SupaComposeDsl
class SupabaseClientBuilder @PublishedApi internal constructor() {

    lateinit var supabaseUrl: String
    lateinit var supabaseKey: String
    var useHTTPS = true
    var httpEngine: HttpClientEngine? = null
    private val httpConfigOverrides = mutableListOf<HttpClientConfig<*>.() -> Unit>()
    private val plugins = mutableMapOf<String, ((SupabaseClient) -> SupabasePlugin)>()

    @PublishedApi
    internal fun build(): SupabaseClient {
        if(!::supabaseKey.isInitialized || supabaseKey.isBlank()) throw IllegalArgumentException("Supabase key is not set")
        if(!::supabaseUrl.isInitialized || supabaseUrl.isBlank()) throw IllegalArgumentException("Supabase url is not set")
        return SupabaseClientImpl(supabaseUrl.split("//").last(), supabaseKey, plugins, httpConfigOverrides, useHTTPS, httpEngine)
    }

    /**
     * Add your own http configuration to [SupabaseClient.httpClient]
     */
    fun httpConfig(block: HttpClientConfig<*>.() -> Unit) {
        httpConfigOverrides.add(block)
    }

    /**
     * Installs a plugin to the [SupabaseClient]
     *
     * Plugins can be either retrieved by calling [PluginManager.getPlugin] within your [SupabaseClient] instance or by using the corresponding **SupabaseClient.plugin** function.
     */
    fun <Config, PluginInstance : SupabasePlugin, Provider : SupabasePluginProvider<Config, PluginInstance>> install(plugin: Provider, init: Config.() -> Unit = {}) {
        val config = plugin.createConfig(init)
        plugin.setup(this, config)
        plugins[plugin.key] = {
            plugin.create(it, config)
        }
    }

}

/**
 * Creates a new [SupabaseClient] instance using [builder]
 */
inline fun createSupabaseClient(builder: SupabaseClientBuilder.() -> Unit) = SupabaseClientBuilder().apply(builder).build()
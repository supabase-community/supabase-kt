package io.github.jan.supacompose

import io.github.jan.supacompose.annotiations.SupaComposeDsl
import io.github.jan.supacompose.plugins.SupacomposePlugin
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine

@SupaComposeDsl
class SupabaseClientBuilder {

    lateinit var supabaseUrl: String
    lateinit var supabaseKey: String
    var useHTTPS = true
    var httpEngine: HttpClientEngine? = null
    private val httpConfigOverrides = mutableListOf<HttpClientConfig<*>.() -> Unit>()
    private val plugins = mutableMapOf<String, ((SupabaseClient) -> Any)>()

    @PublishedApi
    internal fun build(): SupabaseClient {
        if(!::supabaseKey.isInitialized || supabaseKey.isBlank()) throw IllegalArgumentException("Supabase key is not set")
        if(!::supabaseUrl.isInitialized || supabaseUrl.isBlank()) throw IllegalArgumentException("Supabase url is not set")
        return SupabaseClientImpl(supabaseUrl.split("//").last(), supabaseKey, plugins, httpConfigOverrides, useHTTPS, httpEngine)
    }

    fun httpConfig(block: HttpClientConfig<*>.() -> Unit) {
        httpConfigOverrides.add(block)
    }

    fun <C, O : Any, P : SupacomposePlugin<C, O>> install(plugin: P, init: C.() -> Unit = {}) {
        val config = plugin.createConfig(init)
        plugin.setup(this, config)
        plugins[plugin.key] = {
            plugin.create(it, config)
        }
    }

}

inline fun createSupabaseClient(builder: SupabaseClientBuilder.() -> Unit) = SupabaseClientBuilder().apply(builder).build()
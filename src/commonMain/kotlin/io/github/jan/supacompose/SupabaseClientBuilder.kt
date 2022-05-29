package io.github.jan.supacompose

import io.github.jan.supacompose.annotiations.SupaComposeDsl
import io.github.jan.supacompose.plugins.SupacomposePlugin
import io.ktor.client.HttpClientConfig

@SupaComposeDsl
class SupabaseClientBuilder {

    lateinit var supabaseUrl: String
    lateinit var supabaseKey: String
    var useHTTPS = true
    private val httpConfigOverrides = mutableListOf<HttpClientConfig<*>.() -> Unit>()
    private val plugins = mutableMapOf<String, ((SupabaseClient) -> Any)>()

    @PublishedApi
    internal fun build(): SupabaseClient {
        if(!::supabaseKey.isInitialized || supabaseKey.isBlank()) throw IllegalArgumentException("Supabase key is not set")
        if(!::supabaseUrl.isInitialized || supabaseUrl.isBlank()) throw IllegalArgumentException("Supabase url is not set")
        return SupabaseClientImpl(supabaseUrl, supabaseKey, plugins, httpConfigOverrides, useHTTPS)
    }

    fun httpConfig(block: HttpClientConfig<*>.() -> Unit) {
        httpConfigOverrides.add(block)
    }

    fun <C, O : Any, P : SupacomposePlugin<C, O>> install(plugin: P, config: C.() -> Unit = {}) {
        plugin.setup(this, config)
        plugins[plugin.key] = {
            plugin.create(it, config)
        }
    }

}

inline fun createSupabaseClient(builder: SupabaseClientBuilder.() -> Unit) = SupabaseClientBuilder().also(builder).build()
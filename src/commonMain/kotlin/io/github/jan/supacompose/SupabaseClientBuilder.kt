package io.github.jan.supacompose

import io.github.jan.supacompose.annotiations.SupaComposeDsl
import io.github.jan.supacompose.plugins.SupabasePlugin

@SupaComposeDsl
class SupabaseClientBuilder {

    lateinit var supabaseUrl: String
    lateinit var supabaseKey: String
    private val plugins = mutableMapOf<String, ((SupabaseClient) -> Any)>()

    @PublishedApi
    internal fun build(): SupabaseClient = SupabaseClientImpl(supabaseUrl, supabaseKey, plugins)

    fun <C, O : Any, P : SupabasePlugin<C, O>> install(plugin: P, config: C.() -> Unit = {}) {
        plugins[plugin.key] = {
            plugin.create(it, config)
        }
    }

}

inline fun createSupabaseClient(builder: SupabaseClientBuilder.() -> Unit) = SupabaseClientBuilder().also(builder).build()
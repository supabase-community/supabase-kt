package io.github.jan.supacompose.postgrest

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.plugins.SupacomposePlugin
import io.github.jan.supacompose.plugins.SupacomposePluginProvider
import io.github.jan.supacompose.postgrest.query.PostgrestBuilder

sealed interface Postgrest : SupacomposePlugin {

    fun from(table: String): PostgrestBuilder

    operator fun get(table: String): PostgrestBuilder = from(table)

    data class Config(val customPostgrestUrl: String? = null)

    companion object : SupacomposePluginProvider<Config, Postgrest> {

        override val key = "postgrest"
        override fun createConfig(init: Config.() -> Unit) = Config().apply(init)
        override fun create(supabaseClient: SupabaseClient, config: Config): Postgrest {
            return PostgrestImpl(supabaseClient)
        }

    }

}

internal class PostgrestImpl(private val supabaseClient: SupabaseClient) : Postgrest {

    override fun from(table: String): PostgrestBuilder {
        return PostgrestBuilder(supabaseClient, table)
    }

}

/**
 * With the postgrest plugin you can directly interact with your database via an api
 */
val SupabaseClient.postgrest: Postgrest
    get() = pluginManager.getPlugin(Postgrest.key)
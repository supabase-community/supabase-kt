package io.github.jan.supacompose.postgrest

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.plugins.MainConfig
import io.github.jan.supacompose.plugins.MainPlugin
import io.github.jan.supacompose.plugins.SupacomposePlugin
import io.github.jan.supacompose.plugins.SupacomposePluginProvider
import io.github.jan.supacompose.postgrest.query.PostgrestBuilder

sealed interface Postgrest : MainPlugin<Postgrest.Config> {

    fun from(table: String): PostgrestBuilder

    operator fun get(table: String): PostgrestBuilder = from(table)

    data class Config(override var customUrl: String? = null): MainConfig

    companion object : SupacomposePluginProvider<Config, Postgrest> {

        override val key = "rest"
        const val API_VERSION = 1

        override fun createConfig(init: Config.() -> Unit) = Config().apply(init)
        override fun create(supabaseClient: SupabaseClient, config: Config): Postgrest {
            return PostgrestImpl(supabaseClient, config)
        }

    }

}

internal class PostgrestImpl(override val supabaseClient: SupabaseClient, override val config: Postgrest.Config) : Postgrest {

    override val API_VERSION: Int
        get() = Postgrest.API_VERSION

    override val PLUGIN_KEY: String
        get() = Postgrest.key

    override fun from(table: String): PostgrestBuilder {
        return PostgrestBuilder(this, table)
    }

}

/**
 * With the postgrest plugin you can directly interact with your database via an api
 */
val SupabaseClient.postgrest: Postgrest
    get() = pluginManager.getPlugin(Postgrest.key)
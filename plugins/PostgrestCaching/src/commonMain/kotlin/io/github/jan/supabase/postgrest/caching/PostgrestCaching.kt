package io.github.jan.supabase.postgrest.caching

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.decode
import io.github.jan.supabase.plugins.CustomSerializationConfig
import io.github.jan.supabase.plugins.CustomSerializationPlugin
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider

class PostgrestCaching private constructor(
    private val config: Config,
    val supabaseClient: SupabaseClient
): SupabasePlugin, CustomSerializationPlugin {

    class Config(override var serializer: SupabaseSerializer? = null) : CustomSerializationConfig

    override val serializer: SupabaseSerializer = config.serializer ?: supabaseClient.defaultSerializer

    inline fun <reified Data> from(schema: String, table: String): CachableTable<Data> {
        return CachableTable(
            table,
            schema,
            supabaseClient,
            { serializer.decode(it) },
            { serializer.decode(it) }
        )
    }

    inline fun <reified Data> from(table: String): CachableTable<Data> = from("public", table)


    companion object : SupabasePluginProvider<Config, PostgrestCaching> {

        override val key: String = "postgrest-caching"

        override fun create(supabaseClient: SupabaseClient, config: Config): PostgrestCaching {
            return PostgrestCaching(config, supabaseClient)
        }


        override fun createConfig(init: Config.() -> Unit): Config {
            return Config().apply(init)
        }
    }

}

val SupabaseClient.postgrestCaching: PostgrestCaching
    get() = pluginManager.getPlugin(PostgrestCaching)
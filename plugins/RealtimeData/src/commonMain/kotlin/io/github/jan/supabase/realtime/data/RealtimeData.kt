package io.github.jan.supabase.realtime.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.decode
import io.github.jan.supabase.plugins.CustomSerializationConfig
import io.github.jan.supabase.plugins.CustomSerializationPlugin
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider

class RealtimeData private constructor(
    config: Config,
    val supabaseClient: SupabaseClient
): SupabasePlugin, CustomSerializationPlugin {

    class Config(override var serializer: SupabaseSerializer? = null) : CustomSerializationConfig

    override val serializer: SupabaseSerializer = config.serializer ?: supabaseClient.defaultSerializer

    inline fun <reified Data> from(schema: String, table: String): RealtimeTable<Data> {
        return RealtimeTable(
            table,
            schema,
            supabaseClient,
            { serializer.decode(it) },
            { serializer.decode(it) }
        )
    }

    inline fun <reified Data> from(table: String): RealtimeTable<Data> = from("public", table)


    companion object : SupabasePluginProvider<Config, RealtimeData> {

        override val key: String = "realtime-data"

        override fun create(supabaseClient: SupabaseClient, config: Config): RealtimeData {
            return RealtimeData(config, supabaseClient)
        }


        override fun createConfig(init: Config.() -> Unit): Config {
            return Config().apply(init)
        }
    }

}

val SupabaseClient.realtimeData: RealtimeData
    get() = pluginManager.getPlugin(RealtimeData)
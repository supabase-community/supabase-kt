package io.github.jan.supabase.postgrest

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.plugins.MainConfig
import io.github.jan.supabase.plugins.MainPlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.PostgrestBuilder
import io.github.jan.supabase.postgrest.query.PostgrestFilterBuilder
import io.github.jan.supabase.postgrest.request.PostgrestRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Plugin to interact with the supabase Postgrest API
 */
sealed interface Postgrest : MainPlugin<Postgrest.Config> {

    fun from(table: String): PostgrestBuilder

    operator fun get(table: String): PostgrestBuilder = from(table)

    data class Config(override var customUrl: String? = null, override var jwtToken: String? = null): MainConfig

    companion object : SupabasePluginProvider<Config, Postgrest> {

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

/**
 * Executes a database function
 *
 * @param function The name of the function
 * @param parameters The parameters for the function
 * @param head If true, select will delete the selected data.
 * @param count Count algorithm to use to count rows in a table.
 * @param filter Filter the result
 */
suspend inline fun <reified T> Postgrest.rpc(
    function: String,
    parameters: T,
    head: Boolean = false,
    count: Count? = null,
    json: Json = Json,
    filter: PostgrestFilterBuilder.() -> Unit = {}
) = PostgrestRequest.RPC(head, count, PostgrestFilterBuilder().apply(filter).params, if(parameters is JsonElement) parameters else json.encodeToJsonElement(parameters)).execute("rpc/$function", this)

/**
 * Executes a database function
 *
 * @param function The name of the function
 * @param head If true, select will delete the selected data.
 * @param count Count algorithm to use to count rows in a table.
 * @param filter Filter the result
 */
suspend inline fun Postgrest.rpc(
    function: String,
    head: Boolean = false,
    count: Count? = null,
    filter: PostgrestFilterBuilder.() -> Unit = {}
) = PostgrestRequest.RPC(head, count, PostgrestFilterBuilder().apply(filter).params).execute("rpc/$function", this)
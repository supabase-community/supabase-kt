package io.github.jan.supabase.postgrest

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.logging.SupabaseLogger
import io.github.jan.supabase.plugins.CustomSerializationConfig
import io.github.jan.supabase.plugins.CustomSerializationPlugin
import io.github.jan.supabase.plugins.MainConfig
import io.github.jan.supabase.plugins.MainPlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder
import io.github.jan.supabase.postgrest.query.PostgrestUpdate
import io.github.jan.supabase.postgrest.query.request.RpcRequestBuilder
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.serialization.json.JsonObject

/**
 * Plugin to interact with the Supabase Postgrest API
 *
 * To use it you need to install it to the [SupabaseClient]:
 * ```kotlin
 * val supabase = createSupabaseClient(supabaseUrl, supabaseKey) {
 *    install(Postgrest)
 * }
 * ```
 *
 * then you can use it like this:
 * ```kotlin
 * val product = supabase.postgrest["products"].select {
 *    Product::id eq 2
 * }.decodeSingle<Product>()
 * ```
 */

sealed interface Postgrest : MainPlugin<Postgrest.Config>, CustomSerializationPlugin {

    /**
     * Creates a new [PostgrestQueryBuilder] for the given table
     * @param table The table to use for the requests
     */
    fun from(table: String): PostgrestQueryBuilder

    /**
     * Creates a new [PostgrestQueryBuilder] for the given schema and table
     * @param schema The schema to use for the requests
     * @param table The table to use for the requests
     */
    fun from(schema: String, table: String): PostgrestQueryBuilder

    /**
     * Creates a new [PostgrestQueryBuilder] for the given table
     * @param table The table to use for the requests
     */
    operator fun get(schema: String, table: String): PostgrestQueryBuilder = from(schema, table)

    /**
     * Creates a new [PostgrestQueryBuilder] for the given schema and table
     * @param table The table to use for the requests
     */
    operator fun get(table: String): PostgrestQueryBuilder = from(table)

    /**
     * Executes a database function
     *
     * @param function The name of the function
     * @param request Filter the result
     * @throws PostgrestRestException if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun rpc(
        function: String,
        request: RpcRequestBuilder.() -> Unit = {}
    ): PostgrestResult

    /**
     * Executes a database function
     *
     * @param function The name of the function
     * @param parameters The parameters for the function
     * @param request Filter the result
     * @throws PostgrestRestException if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun rpc(
        function: String,
        parameters: JsonObject,
        request: RpcRequestBuilder.() -> Unit = {},
    ): PostgrestResult

    /**
     * Config for the Postgrest plugin
     * @param defaultSchema The default schema to use for the requests. Defaults to "public"
     * @param propertyConversionMethod The method to use to convert the property names to the column names in [PostgrestRequestBuilder] and [PostgrestUpdate]. Defaults to [PropertyConversionMethod.CAMEL_CASE_TO_SNAKE_CASE]
     */
    data class Config(
        var defaultSchema: String = "public",
        var propertyConversionMethod: PropertyConversionMethod = PropertyConversionMethod.CAMEL_CASE_TO_SNAKE_CASE,
    ): MainConfig(), CustomSerializationConfig {

        override var serializer: SupabaseSerializer? = null

    }

    companion object : SupabasePluginProvider<Config, Postgrest> {

        override val key = "rest"

        override val logger: SupabaseLogger = SupabaseClient.createLogger("Supabase-PostgREST")

        /**
         * The current postgrest API version
         */
        const val API_VERSION = 1

        override fun createConfig(init: Config.() -> Unit) = Config().apply(init)
        override fun create(supabaseClient: SupabaseClient, config: Config): Postgrest {
            return PostgrestImpl(supabaseClient, config)
        }

    }

}


/**
 * With the postgrest plugin you can directly interact with your database via an api
 */
val SupabaseClient.postgrest: Postgrest
    get() = pluginManager.getPlugin(Postgrest)

/**
 * Creates a new [PostgrestQueryBuilder] for the given table
 * @param table The table to use for the requests
 */
fun SupabaseClient.from(table: String): PostgrestQueryBuilder = pluginManager.getPlugin(Postgrest).from(table)

/**
 * Creates a new [PostgrestQueryBuilder] for the given schema and table
 * @param schema The schema to use for the requests
 * @param table The table to use for the requests
 */
fun SupabaseClient.from(schema: String, table: String): PostgrestQueryBuilder = pluginManager.getPlugin(Postgrest).from(schema, table)
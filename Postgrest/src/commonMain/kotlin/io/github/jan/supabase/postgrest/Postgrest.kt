package io.github.jan.supabase.postgrest

import io.github.jan.supabase.KotlinXSupabaseSerializer
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.bodyOrNull
import io.github.jan.supabase.encodeToJsonElement
import io.github.jan.supabase.exceptions.BadRequestRestException
import io.github.jan.supabase.exceptions.NotFoundRestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.UnauthorizedRestException
import io.github.jan.supabase.exceptions.UnknownRestException
import io.github.jan.supabase.gotrue.authenticatedSupabaseApi
import io.github.jan.supabase.plugins.CustomSerializationPlugin
import io.github.jan.supabase.plugins.MainConfig
import io.github.jan.supabase.plugins.MainPlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.PostgrestBuilder
import io.github.jan.supabase.postgrest.query.PostgrestFilterBuilder
import io.github.jan.supabase.postgrest.query.PostgrestUpdate
import io.github.jan.supabase.postgrest.request.PostgrestRequest
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonElement

/**
 * Plugin to interact with the supabase Postgrest API
 *
 * To use it you need to install it to the [SupabaseClient]:
 * ```kotlin
 * val client = createSupabaseClient(supabaseUrl, supabaseKey) {
 *    install(Postgrest)
 * }
 * ```
 *
 * then you can use it like this:
 * ```kotlin
 * val product = client.postgrest["products"].select {
 *    Product::id eq 2
 * }.decodeSingle<Product>()
 * ```
 */
sealed interface Postgrest : MainPlugin<Postgrest.Config> {

    /**
     * Creates a new [PostgrestBuilder] for the given table
     * @param table The table to use for the requests
     */
    fun from(table: String): PostgrestBuilder

    /**
     * Creates a new [PostgrestBuilder] for the given schema and table
     * @param schema The schema to use for the requests
     * @param table The table to use for the requests
     */
    fun from(schema: String, table: String): PostgrestBuilder

    /**
     * Creates a new [PostgrestBuilder] for the given table
     * @param table The table to use for the requests
     */
    operator fun get(schema: String, table: String): PostgrestBuilder = from(schema, table)

    /**
     * Creates a new [PostgrestBuilder] for the given schema and table
     * @param schema The schema to use for the requests
     * @param table The table to use for the requests
     */
    operator fun get(table: String): PostgrestBuilder = from(table)

    /**
     * Config for the Postgrest plugin
     * @param defaultSchema The default schema to use for the requests. Defaults to "public"
     * @param propertyConversionMethod The method to use to convert the property names to the column names in [PostgrestFilterBuilder] and [PostgrestUpdate]. Defaults to [PropertyConversionMethod.CAMEL_CASE_TO_SNAKE_CASE]
     */
    data class Config(
        override var customUrl: String? = null,
        override var jwtToken: String? = null,
        var defaultSchema: String = "public",
        var propertyConversionMethod: PropertyConversionMethod = PropertyConversionMethod.CAMEL_CASE_TO_SNAKE_CASE,
        override var serializer: SupabaseSerializer = KotlinXSupabaseSerializer()
    ): MainConfig, CustomSerializationPlugin

    companion object : SupabasePluginProvider<Config, Postgrest> {

        override val key = "rest"

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

internal class PostgrestImpl(override val supabaseClient: SupabaseClient, override val config: Postgrest.Config) : Postgrest {

    override val apiVersion: Int
        get() = Postgrest.API_VERSION

    override val pluginKey: String
        get() = Postgrest.key

    @OptIn(SupabaseInternal::class)
    val api = supabaseClient.authenticatedSupabaseApi(this)

    override fun from(table: String): PostgrestBuilder {
        return PostgrestBuilder(this, table)
    }

    override fun from(schema: String, table: String): PostgrestBuilder {
        return PostgrestBuilder(this, table, schema)
    }

    override suspend fun parseErrorResponse(response: HttpResponse): RestException {
        val body = response.bodyOrNull<PostgrestErrorResponse>() ?: PostgrestErrorResponse("Unknown error")
        return when(response.status) {
            HttpStatusCode.Unauthorized -> UnauthorizedRestException(body.message, response, body.details ?: body.hint)
            HttpStatusCode.NotFound -> NotFoundRestException(body.message, response, body.details ?: body.hint)
            HttpStatusCode.BadRequest -> BadRequestRestException(body.message, response, body.details ?: body.hint)
            else -> UnknownRestException(body.message, response, body.details ?: body.hint)
        }
    }

}

/**
 * With the postgrest plugin you can directly interact with your database via an api
 */
val SupabaseClient.postgrest: Postgrest
    get() = pluginManager.getPlugin(Postgrest)

/**
 * Executes a database function
 *
 * @param function The name of the function
 * @param parameters The parameters for the function
 * @param head If true, select will delete the selected data.
 * @param count Count algorithm to use to count rows in a table.
 * @param filter Filter the result
 * @throws RestException or one of its subclasses if the request failed
 */
suspend inline fun <reified T : Any> Postgrest.rpc(
    function: String,
    parameters: T,
    head: Boolean = false,
    count: Count? = null,
    filter: PostgrestFilterBuilder.() -> Unit = {}
) = PostgrestRequest.RPC(head, count, PostgrestFilterBuilder(config.propertyConversionMethod).apply(filter).params, if(parameters is JsonElement) parameters else config.serializer.encodeToJsonElement(parameters)).execute("rpc/$function", this)

/**
 * Executes a database function
 *
 * @param function The name of the function
 * @param head If true, select will delete the selected data.
 * @param count Count algorithm to use to count rows in a table.
 * @param filter Filter the result
 * @throws RestException or one of its subclasses if the request failed
 */
suspend inline fun Postgrest.rpc(
    function: String,
    head: Boolean = false,
    count: Count? = null,
    filter: PostgrestFilterBuilder.() -> Unit = {}
) = PostgrestRequest.RPC(head, count, PostgrestFilterBuilder(config.propertyConversionMethod).apply(filter).params).execute("rpc/$function", this)
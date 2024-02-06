package io.github.jan.supabase.functions

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.encode
import io.github.jan.supabase.exceptions.BadRequestRestException
import io.github.jan.supabase.exceptions.NotFoundRestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.UnauthorizedRestException
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.authenticatedSupabaseApi
import io.github.jan.supabase.logging.SupabaseLogger
import io.github.jan.supabase.plugins.CustomSerializationConfig
import io.github.jan.supabase.plugins.CustomSerializationPlugin
import io.github.jan.supabase.plugins.MainConfig
import io.github.jan.supabase.plugins.MainPlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

/**
 * Plugin to interact with the supabase Edge Functions API
 *
 * To use it you need to install it to the [SupabaseClient]:
 * ```kotlin
 * val supabase = createSupabaseClient(supabaseUrl, supabaseKey) {
 *    install(Functions)
 * }
 * ```
 *
 * then you can use it like this:
 * ```kotlin
 * val response = supabase.functions("myFunction")
 * //or store it in a variable
 * val function = supabase.functions.buildEdgeFunction("myFunction")
 * val response = function()
 * ```
 */
class Functions(override val config: Config, override val supabaseClient: SupabaseClient) : MainPlugin<Functions.Config>, CustomSerializationPlugin {

    override val apiVersion: Int
        get() = API_VERSION

    override val pluginKey: String
        get() = key

    override val logger: SupabaseLogger = config.logger(config.logLevel ?: supabaseClient.logLevel)

    override val serializer = config.serializer ?: supabaseClient.defaultSerializer

    @OptIn(SupabaseInternal::class)
    @PublishedApi
    internal val api = supabaseClient.authenticatedSupabaseApi(this)

    /**
     * Invokes a remote edge function. The authorization token is automatically added to the request.
     * @param function The function to invoke
     * @param builder The request builder to configure the request
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline operator fun invoke(function: String, crossinline builder: HttpRequestBuilder.() -> Unit): HttpResponse {
        return api.post(function, builder)
    }

    /**
     * Invokes a remote edge function. The authorization token is automatically added to the request.
     * Note, if you want to serialize [body] to json, you need to add the [HttpHeaders.ContentType] header yourself.
     * @param function The function to invoke
     * @param body The body of the request
     * @param headers Headers to add to the request
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline operator fun <reified T : Any> invoke(function: String, body: T, headers: Headers = Headers.Empty): HttpResponse = invoke(function) {
        this.headers.appendAll(headers)
        setBody(serializer.encode(body))
    }

    /**
     * Invokes a remote edge function. The authorization token is automatically added to the request.
     * @param function The function to invoke
     * @param headers Headers to add to the request
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline operator fun invoke(function: String, headers: Headers = Headers.Empty): HttpResponse = invoke(function) {
        this.headers.appendAll(headers)
    }

    /**
     * Builds an [EdgeFunction] which can be invoked multiple times
     * @param function The function name
     * @param headers Headers to add to the requests when invoking the function
     */
    @OptIn(SupabaseInternal::class)
    fun buildEdgeFunction(function: String, headers: Headers = Headers.Empty) = EdgeFunction(function, headers, supabaseClient)

    override suspend fun parseErrorResponse(response: HttpResponse): RestException {
        val error = response.bodyAsText()
        return when(response.status) {
            HttpStatusCode.Unauthorized -> UnauthorizedRestException(error, response)
            HttpStatusCode.NotFound -> NotFoundRestException(error, response)
            HttpStatusCode.BadRequest -> BadRequestRestException(error, response)
            else -> UnauthorizedRestException(error, response)
        }
    }

    /**
     * The config for the [Functions] plugin
     * @param customUrl A custom url to use for the requests. If not provided, the default url will be used
     * @param jwtToken A jwt token to use for the requests. If not provided, the token from the [Auth] plugin, or the supabaseKey will be used
     * @property serializer A serializer used for serializing/deserializing objects e.g. in [Functions.invoke] or [EdgeFunction.invoke]. Defaults to [KotlinXSerializer]
     */
    class Config : MainConfig(), CustomSerializationConfig {

        override var serializer: SupabaseSerializer? = null

    }

    companion object : SupabasePluginProvider<Config, Functions> {

        override val key = "functions"

        /**
         * The current functions api version
         */
        const val API_VERSION = 1

        override fun create(supabaseClient: SupabaseClient, config: Config): Functions {
            return Functions(config, supabaseClient)
        }

        override fun createConfig(init: Config.() -> Unit) = Config().apply(init)


    }

}

/**
 * The Functions plugin handles everything related to supabase's edge functions
 */
val SupabaseClient.functions: Functions
    get() = pluginManager.getPlugin(Functions)
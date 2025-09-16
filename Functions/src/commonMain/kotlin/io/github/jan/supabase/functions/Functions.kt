package io.github.jan.supabase.functions

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.authenticatedSupabaseApi
import io.github.jan.supabase.encode
import io.github.jan.supabase.exceptions.BadRequestRestException
import io.github.jan.supabase.exceptions.NotFoundRestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.UnauthorizedRestException
import io.github.jan.supabase.plugins.CustomSerializationConfig
import io.github.jan.supabase.plugins.CustomSerializationPlugin
import io.github.jan.supabase.plugins.MainConfig
import io.github.jan.supabase.plugins.MainPlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
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

    override val serializer = config.serializer ?: supabaseClient.defaultSerializer

    @OptIn(SupabaseInternal::class)
    @PublishedApi
    internal val api = supabaseClient.authenticatedSupabaseApi(this)

    /**
     * Invokes a remote edge function. The authorization token is automatically added to the request.
     * @param function The function to invoke. If name of the function is renamed, use the slug after URL
     * @param builder The request builder to configure the request
     * @param region The region where the function is invoked
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline operator fun invoke(function: String, region: FunctionRegion = config.defaultRegion, crossinline builder: HttpRequestBuilder.() -> Unit): HttpResponse {
        return api.post(function) {
            builder()
            header("x-region", region.value)
        }
    }

    /**
     * Invokes a remote edge function. The authorization token is automatically added to the request.
     * Note, if you want to serialize [body] to json, you need to add the [HttpHeaders.ContentType] header yourself.
     * @param function The function to invoke. If name of the function is renamed, use the slug after URL
     * @param body The body of the request
     * @param headers Headers to add to the request
     * @param region The region where the function is invoked
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline operator fun <reified T : Any> invoke(function: String, body: T, region: FunctionRegion = config.defaultRegion, headers: Headers = Headers.Empty): HttpResponse = invoke(function) {
        this.headers.appendAll(headers)
        header("x-region", region.value)
        setBody(serializer.encode(body))
    }

    /**
     * Invokes a remote edge function. The authorization token is automatically added to the request.
     * @param function The function to invoke. If name of the function is renamed, use the slug after URL
     * @param headers Headers to add to the request
     * @param region The region where the function is invoked
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend inline operator fun invoke(function: String, region: FunctionRegion = config.defaultRegion, headers: Headers = Headers.Empty): HttpResponse = invoke(function) {
        this.headers.appendAll(headers)
        header("x-region", region.value)
    }

    /**
     * Builds an [EdgeFunction] which can be invoked multiple times
     * @param function The function name. If name of the function is renamed, use the slug after URL
     * @param headers Headers to add to the requests when invoking the function
     * @param region The region where the function is invoked
     */
    @OptIn(SupabaseInternal::class)
    fun buildEdgeFunction(
        function: String,
        region: FunctionRegion = config.defaultRegion,
        headers: Headers = Headers.Empty
    ) = EdgeFunction(
        functionName = function,
        headers = Headers.build {
            appendAll(headers)
            append("x-region", region.value)
        },
        supabaseClient = supabaseClient
    )

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

        /**
         * The default region to use when invoking a function
         */
        var defaultRegion: FunctionRegion = FunctionRegion.ANY

    }

    /**
     * @see Functions
     */
    companion object : SupabasePluginProvider<Config, Functions> {

        override val key = "functions"

        override val logger = SupabaseClient.createLogger("Supabase-Functions")

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
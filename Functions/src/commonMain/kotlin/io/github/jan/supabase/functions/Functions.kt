package io.github.jan.supabase.functions

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotiations.SupabaseInternal
import io.github.jan.supabase.buildUrl
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.authenticatedSupabaseApi
import io.github.jan.supabase.gotrue.checkErrors
import io.github.jan.supabase.plugins.MainConfig
import io.github.jan.supabase.plugins.MainPlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.appendEncodedPathSegments

/**
 * Plugin to interact with the supabase Edge Functions API
 */
class Functions(override val config: Config, override val supabaseClient: SupabaseClient) : MainPlugin<Functions.Config> {

    override val API_VERSION: Int
        get() = Functions.API_VERSION

    override val PLUGIN_KEY: String
        get() = key

    private val baseUrl = supabaseClient.supabaseHttpUrl.replaceFirst(".", ".functions.")

    @PublishedApi
    internal val api = supabaseClient.authenticatedSupabaseApi(this)

    /**
     * Invokes a remote edge function. The authorization token is automatically added to the request.
     * @param function The function to invoke
     * @param builder The request builder to configure the request
     */
    suspend inline operator fun invoke(function: String, crossinline builder: HttpRequestBuilder.() -> Unit): HttpResponse {
        return api.post(function) {
            val token = config.jwtToken ?: supabaseClient.pluginManager.getPluginOrNull(GoTrue)?.currentAccessTokenOrNull()
            token.let {
                this.headers[HttpHeaders.Authorization] = "Bearer $it"
            }
            builder()
        }.checkErrors("Couldn't invoke function $function")
    }

    /**
     * Invokes a remote edge function. The authorization token is automatically added to the request.
     * Note, if you want to serialize [body] to json, you need to add the [HttpHeaders.ContentType] header yourself.
     * @param function The function to invoke
     * @param body The body of the request
     * @param headers Headers to add to the request
     */
    suspend inline operator fun <reified T> invoke(function: String, body: T, headers: Headers = Headers.Empty): HttpResponse = invoke(function) {
        this.headers.appendAll(headers)
        body?.let {
            setBody(body)
        }
    }

    /**
     * Invokes a remote edge function. The authorization token is automatically added to the request.
     * @param function The function to invoke
     * @param headers Headers to add to the request
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

    override fun resolveUrl(path: String): String {
        return buildUrl(config.customUrl ?: baseUrl) {
            appendEncodedPathSegments(path)
        }
    }

    data class Config(
        override var customUrl: String? = null,
        override var jwtToken: String? = null
    ) : MainConfig

    companion object : SupabasePluginProvider<Config, Functions> {

        override val key = "functions"
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
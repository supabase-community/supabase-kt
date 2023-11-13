package io.github.jan.supabase.plugins

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.buildUrl
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.appendEncodedPathSegments

/**
 * Config for [MainPlugin]s
 */
interface MainConfig {

    /**
     * The url used for this module
     */
    var customUrl: String?

    /**
     * The jwt token used for this module. If null, the client will use the token from GoTrue's current session
     */
    var jwtToken: String?

}

/**
 * Represents main plugins like Auth or Functions
 */
interface MainPlugin <Config : MainConfig> : SupabasePlugin {

    /**
     * The configuration for this plugin
     */
    val config: Config

    /**
     * The corresponding [SupabaseClient] instance
     */
    val supabaseClient: SupabaseClient

    /**
     * The version for the api the plugin is using
     */
    val apiVersion: Int

    /**
     * The unique key for this plugin
     */
    val pluginKey: String

    /**
     * Gets the auth url from either [config.customUrl] or [SupabaseClient.supabaseHttpUrl] and adds [path] to it
     */
    @OptIn(SupabaseInternal::class)
    fun resolveUrl(path: String = ""): String {
        val isBase = config.customUrl == null
        return buildUrl(config.customUrl ?: supabaseClient.supabaseHttpUrl) {
            if(isBase) {
                appendEncodedPathSegments(pluginKey, "v${apiVersion}")
            }
            if(path.isNotBlank()) {
                appendEncodedPathSegments(path)
            }
        }
    }

    /**
     * Parses the response from the server and builds a [RestException]
     */
    suspend fun parseErrorResponse(response: HttpResponse): RestException

    @SupabaseInternal
    fun init() {}

}

/**
 * Creates a standalone Supabase Module. Uses an underlying [SupabaseClient]
 * @param provider The provider for this module. E.g. GoTrue, Realtime or Storage
 * @param url The url for this module
 * @param apiKey The api key for this module
 * @param config The configuration for this module
 */
@Deprecated("Use createSupabaseClient instead", level = DeprecationLevel.ERROR)
inline fun <Config : MainConfig, reified Plugin : MainPlugin<Config>> standaloneSupabaseModule(provider: SupabasePluginProvider<Config, Plugin>, url: String, apiKey: String? = null, crossinline config: Config.() -> Unit = {}): Plugin {
    val underlyingClient = createSupabaseClient("", apiKey ?: "") {
        install(provider) {
            customUrl = url
            config()
        }
    }
    return underlyingClient.pluginManager.getPlugin(provider)
}
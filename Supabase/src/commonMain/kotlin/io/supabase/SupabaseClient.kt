package io.supabase

import io.supabase.annotations.SupabaseInternal
import io.supabase.logging.KermitSupabaseLogger
import io.supabase.logging.LogLevel
import io.supabase.logging.SupabaseLogger
import io.supabase.logging.i
import io.supabase.network.KtorSupabaseHttpClient
import io.supabase.plugins.PluginManager
import io.supabase.plugins.SupabasePlugin
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine

/**
 * The main class to interact with Supabase.
 *
 * To add functionality, add plugins like **Auth** or **Functions** within the [SupabaseClientBuilder]
 */
sealed interface SupabaseClient {

    /**
     * The supabase url with either a http or https scheme.
     */
    val supabaseHttpUrl: String

    /**
     * The base supabase url without any scheme
     */
    val supabaseUrl: String

    /**
     * The api key for interacting with the supabase api
     */
    val supabaseKey: String

    /**
     * The plugin manager is used to manage installed plugins
     */
    val pluginManager: PluginManager

    /**
     * The http client used to interact with the Supabase api
     */
    val httpClient: KtorSupabaseHttpClient

    /**
     * Whether [supabaseHttpUrl] uses https
     */
    val useHTTPS: Boolean

    /**
     * The default serializer used to serialize and deserialize custom data types.
     */
    val defaultSerializer: SupabaseSerializer

    /**
     * The custom access token provider used to provide custom access tokens for requests. Configured within the [SupabaseClientBuilder]
     */
    @SupabaseInternal
    val accessToken: AccessTokenProvider?

    /**
     * Releases all resources held by the [httpClient] and all plugins the [pluginManager]
     */
    suspend fun close()

    companion object {

        /**
         * The default logging level used for plugins. Can be changed within the [SupabaseClientBuilder]
         */
        var DEFAULT_LOG_LEVEL = LogLevel.INFO
            internal set

        internal val LOGGER = createLogger("Supabase-Core")

        /**
         * Creates a new [SupabaseLogger] using the [KermitSupabaseLogger] implementation.
         * @param tag The tag for the logger
         * @param level The logging level. If set to null, the [DEFAULT_LOG_LEVEL] property will be used instead
         */
        fun createLogger(tag: String, level: LogLevel? = null) = KermitSupabaseLogger(level, tag)

    }

}

@Suppress("LongParameterList") //TODO: maybe refactor
internal class SupabaseClientImpl(
    override val supabaseUrl: String,
    override val supabaseKey: String,
    plugins: Map<String, (SupabaseClient) -> SupabasePlugin<*>>,
    httpConfigOverrides: MutableList<HttpClientConfig<*>.() -> Unit>,
    override val useHTTPS: Boolean,
    requestTimeout: Long,
    httpEngine: HttpClientEngine?,
    override val defaultSerializer: SupabaseSerializer,
    override val accessToken: AccessTokenProvider?,
) : SupabaseClient {

    init {
        SupabaseClient.LOGGER.i {
            "SupabaseClient created! Please report any bugs you find."
        }
    }

    override val supabaseHttpUrl: String = if (useHTTPS) {
        "https://$supabaseUrl"
    } else {
        "http://$supabaseUrl"
    }

 //   override val coroutineContext = Dispatchers.Default + SupervisorJob()

    @OptIn(SupabaseInternal::class)
    override val httpClient = KtorSupabaseHttpClient(supabaseKey, httpConfigOverrides, requestTimeout, httpEngine)

    override val pluginManager = PluginManager(plugins.toList().associate { (key, value) ->
        key to value(this)
    })

    init {
        pluginManager.installedPlugins.values.forEach(SupabasePlugin<*>::init)
    }

    override suspend fun close() {
        httpClient.close()
        pluginManager.closeAllPlugins()
    }

}


package io.github.jan.supabase

import io.github.jan.supabase.SupabaseClient.Companion.DEFAULT_LOG_LEVEL
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.logging.KermitSupabaseLogger
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.logging.SupabaseLogger
import io.github.jan.supabase.logging.i
import io.github.jan.supabase.network.KtorSupabaseHttpClient
import io.github.jan.supabase.plugins.PluginManager
import io.github.jan.supabase.plugins.SupabasePlugin

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

internal class SupabaseClientImpl(
    config: SupabaseClientConfig,
) : SupabaseClient {

    override val accessToken: AccessTokenProvider? = config.accessToken
    override val defaultSerializer: SupabaseSerializer = config.defaultSerializer
    override val supabaseUrl: String = config.supabaseUrl
    override val supabaseKey: String = config.supabaseKey
    override val useHTTPS: Boolean = config.networkConfig.useHTTPS

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

    @OptIn(SupabaseInternal::class)
    override val httpClient = KtorSupabaseHttpClient(
        supabaseKey,
        config.networkConfig.httpConfigOverrides,
        config.networkConfig.requestTimeout.inWholeMilliseconds,
        config.networkConfig.httpEngine
    )

    override val pluginManager = PluginManager(config.plugins.toList().associate { (key, value) ->
        key to value(this)
    })

    init {
        pluginManager.installedPlugins.values.forEach(SupabasePlugin<*>::init)
    }

    override suspend fun close() {
        SupabaseClient.LOGGER.i { "Closing SupabaseClient" }
        httpClient.close()
        pluginManager.closeAllPlugins()
    }

}


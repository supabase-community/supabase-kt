package io.github.jan.supabase

import io.github.jan.supabase.annotations.SupabaseDsl
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.plugins.PluginManager
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginConfig
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Creates a new [SupabaseClient] with the given options.
 *
 * Use [createSupabaseClient] to create a new instance of [SupabaseClient].
 */
@SupabaseDsl
class SupabaseClientBuilder @PublishedApi internal constructor(private val supabaseUrl: String, private val supabaseKey: String) {

    /**
     * Whether to use HTTPS for network requests with the [supabaseUrl]
     *
     * Default: true
     */
    var useHTTPS = true

    /**
     * Custom ktor http client engine. If null, an engine from the dependencies will be used.
     */
    var httpEngine: HttpClientEngine? = null

    /**
     * Whether to ignore if [supabaseUrl] contains modules like 'realtime' or 'auth'. If false, an exception will be thrown.
     *
     * Default: false
     */
    var ignoreModulesInUrl = false

    /**
     * [Duration] after network requests throw a [HttpRequestTimeoutException]
     *
     * Default: 10 seconds
     */
    var requestTimeout = 10.seconds

    /**
     * The default log level used for plugins.
     *
     * Can be overridden by the plugin's config
     */
    var defaultLogLevel: LogLevel
        set(value) {
            SupabaseClient.DEFAULT_LOG_LEVEL = value
        }
        get() = SupabaseClient.DEFAULT_LOG_LEVEL

    /**
     * The default serializer used to serialize and deserialize custom data types.
     *
     * Default: [KotlinXSerializer]
     */
    var defaultSerializer: SupabaseSerializer = KotlinXSerializer()

    private val httpConfigOverrides = mutableListOf<HttpClientConfig<*>.() -> Unit>()
    private val plugins = mutableMapOf<String, ((SupabaseClient) -> SupabasePlugin<*>)>()

    init {
        val module = when {
            supabaseUrl.contains("realtime/v1") -> "realtime/v1"
            supabaseUrl.contains("auth/v1") -> "auth/v1"
            supabaseUrl.contains("storage/v1") -> "storage/v1"
            supabaseUrl.contains("rest/v1") -> "rest/v1"
            else -> null
        }
        if(!ignoreModulesInUrl && module != null) {
            error("The supabase url should not contain ($module), supabase-kt handles the url endpoints. If you want to use a custom url for a module, specify it within their builder but that's not necessary for normal supabase projects")
        }
        if(supabaseUrl.startsWith("http://")) {
            useHTTPS = false
        }
    }

    @PublishedApi
    internal fun build(): SupabaseClient {
        return SupabaseClientImpl(
            supabaseUrl.split("//").last(),
            supabaseKey,
            plugins,
            httpConfigOverrides,
            useHTTPS,
            requestTimeout.inWholeMilliseconds,
            httpEngine,
            defaultSerializer
        )
    }

    /**
     * Add your own http configuration to [SupabaseClient.httpClient]
     *
     * **Warning:** This is an internal function and should only be used if you know what you are doing. You don't have to specify the HTTP client engine, it will be automatically set by Ktor.
     */
    @SupabaseInternal
    @SupabaseDsl
    fun httpConfig(block: @SupabaseDsl HttpClientConfig<*>.() -> Unit) {
        httpConfigOverrides.add(block)
    }

    /**
     * Installs a plugin to the [SupabaseClient]
     *
     * Plugins can be either retrieved by calling [PluginManager.getPlugin] within your [SupabaseClient] instance or by using the corresponding **SupabaseClient.plugin** extension property.
     */
    @SupabaseDsl
    fun <Config : SupabasePluginConfig, PluginInstance : SupabasePlugin<Config>, Provider : SupabasePluginProvider<Config, PluginInstance>> install(plugin: Provider, init: @SupabaseDsl Config.() -> Unit = {}) {
        val config = plugin.createConfig(init)
        plugin.setup(this, config)
        plugins[plugin.KEY] = {
            plugin.create(it, config)
        }
    }

}

/**
 * Creates a new [SupabaseClient] instance using [builder]
 * @param supabaseUrl The supabase url. Example: 'id.supabase.co' or 'https://id.supabase.co' (no 'https://id.supabase.co/auth/v1')
 * @param supabaseKey The supabase api key
 */
inline fun createSupabaseClient(supabaseUrl: String, supabaseKey: String, builder: SupabaseClientBuilder.() -> Unit) = SupabaseClientBuilder(supabaseUrl, supabaseKey).apply(builder).build()
package io.github.jan.supacompose

import io.github.aakira.napier.Napier
import io.github.jan.supacompose.annotiations.SupaComposeInternal
import io.github.jan.supacompose.plugins.PluginManager
import io.github.jan.supacompose.plugins.SupacomposePlugin
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

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
     * The http client used to interact with the supabase api
     */
    val httpClient: HttpClient

    /**
     * Releases all resources held by the [httpClient] and all plugins the [pluginManager]
     */
    suspend fun close()

    @SupaComposeInternal
    fun path(path: String): String = "$supabaseHttpUrl/$path"

}

internal class SupabaseClientImpl(
    override val supabaseUrl: String,
    override val supabaseKey: String,
    plugins: Map<String, (SupabaseClient) -> SupacomposePlugin>,
    httpConfigOverrides: MutableList<HttpClientConfig<*>.() -> Unit>,
    useHTTPS: Boolean,
    httpEngine: HttpClientEngine?,
) : SupabaseClient {

    init {
        Napier.w {
            "Warning! You are using a alpha version of SupaCompose. Don't use it in a production environment. Please report any bugs you find."
        }
    }

    override val supabaseHttpUrl: String = if (useHTTPS) {
        "https://$supabaseUrl"
    } else {
        "http://$supabaseUrl"
    }

 //   override val coroutineContext = Dispatchers.Default + SupervisorJob()

    override val pluginManager = PluginManager(plugins.toList().associate { (key, value) ->
        key to value(this)
    })

    override val httpClient = if(httpEngine != null) {
        HttpClient(httpEngine) {
            install(DefaultRequest) {
                headers {
                    append("apikey", supabaseKey)
                }
                port = 443
            }
            install(ContentNegotiation) {
                json(supabaseJson)
            }
            httpConfigOverrides.forEach { it.invoke(this) }
        }
    } else {
        HttpClient {
            install(DefaultRequest) {
                headers {
                    append("apikey", supabaseKey)
                }
                port = 443
            }
            install(ContentNegotiation) {
                json(supabaseJson)
            }
            httpConfigOverrides.forEach { it.invoke(this) }
        }
    }

    override suspend fun close() {
        httpClient.close()
        pluginManager.closeAllPlugins()
    }

}


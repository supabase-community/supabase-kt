package io.github.jan.supacompose

import io.github.aakira.napier.Napier
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

sealed interface SupabaseClient: CoroutineScope {

    val supabaseHttpUrl: String
    val supabaseUrl: String
    val supabaseKey: String
    val plugins: Map<String, Any>
    val httpClient: HttpClient

    fun path(path: String): String = "$supabaseHttpUrl/$path"

}

internal class SupabaseClientImpl(
    override val supabaseUrl: String,
    override val supabaseKey: String,
    plugins: Map<String, (SupabaseClient) -> Any>,
    httpConfigOverrides: MutableList<HttpClientConfig<*>.() -> Unit>,
    useHTTPS: Boolean,
    httpEngine: HttpClientEngine?,
) : SupabaseClient {

    override val supabaseHttpUrl: String = if (useHTTPS) {
        "https://$supabaseUrl"
    } else {
        "http://$supabaseUrl"
    }

    init {
        Napier.w {
            "Warning! You are using a alpha version of SupaCompose. Don't use it in a production environment. Please report any bugs you find."
        }
    }
    
    override val coroutineContext = Dispatchers.Default + Job()

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

    override val plugins = plugins.toList().associate { (key, value) ->
        key to value(this)
    }

}


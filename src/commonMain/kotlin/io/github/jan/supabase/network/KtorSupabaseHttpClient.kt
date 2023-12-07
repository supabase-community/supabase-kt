@file:Suppress("UndocumentedPublicFunction")
package io.github.jan.supabase.network

import co.touchlab.kermit.Logger
import io.github.jan.supabase.BuildConfig
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.supabaseJson
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.serialization.kotlinx.json.json

private const val HTTPS_PORT = 443

/**
 * A [SupabaseHttpClient] that uses ktor to send requests
 */
@OptIn(SupabaseInternal::class)
class KtorSupabaseHttpClient @SupabaseInternal constructor(
    private val supabaseKey: String,
    modifiers: List<HttpClientConfig<*>.() -> Unit> = listOf(),
    private val requestTimeout: Long,
    engine: HttpClientEngine? = null,
): SupabaseHttpClient() {

    @SupabaseInternal
    val httpClient =
        if(engine != null) HttpClient(engine) { applyDefaultConfiguration(modifiers) }
        else HttpClient { applyDefaultConfiguration(modifiers) }

    override suspend fun request(url: String, builder: HttpRequestBuilder.() -> Unit): HttpResponse {
        val request = HttpRequestBuilder().apply {
            builder()
        }

        val response = try {
            httpClient.request(url, builder)
        } catch(e: HttpRequestTimeoutException) {
            Logger.d("Core") { "Request timed out after $requestTimeout ms" }
            throw e
        } catch(e: Exception) {
            Logger.d("Core") { "Request failed with ${e.message}" }
            throw HttpRequestException(e.message ?: "", request)
        }
        return response
    }

    override suspend fun prepareRequest(
        url: String,
        builder: HttpRequestBuilder.() -> Unit
    ): HttpStatement {
        val request = HttpRequestBuilder().apply {
            builder()
        }

        val response = try {
            httpClient.prepareRequest(url, builder)
        } catch(e: HttpRequestTimeoutException) {
            Logger.d("Core") { "Request timed out after $requestTimeout ms" }
            throw e
        } catch(e: Exception) {
            Logger.d("Core") { "Request failed with ${e.message}" }
            throw HttpRequestException(e.message ?: "", request)
        }
        return response
    }

    suspend fun webSocketSession(url: String, block: HttpRequestBuilder.() -> Unit = {}) = httpClient.webSocketSession(url, block)

    fun close() = httpClient.close()

    private fun HttpClientConfig<*>.applyDefaultConfiguration(modifiers: List<HttpClientConfig<*>.() -> Unit>) {
        install(DefaultRequest) {
            headers {
                if(supabaseKey.isNotBlank()) {
                    append("apikey", supabaseKey)
                }
                append("X-Client-Info", "supabase-kt/${BuildConfig.PROJECT_VERSION}")
            }
            port = HTTPS_PORT
        }
        install(ContentNegotiation) {
            json(supabaseJson)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = requestTimeout
        }
        modifiers.forEach { it.invoke(this) }
    }

}
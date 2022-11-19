package io.github.jan.supabase.network

import io.github.aakira.napier.Napier
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
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.content.TextContent
import io.ktor.serialization.kotlinx.json.json

/**
 * A [SupabaseHttpClient] that uses ktor to send requests
 */
class KtorSupabaseHttpClient(
    private val supabaseKey: String,
    modifiers: List<HttpClientConfig<*>.() -> Unit> = listOf(),
    private val logNetworkTraffic: Boolean,
    private val requestTimeout: Long,
    engine: HttpClientEngine? = null,
): SupabaseHttpClient() {

    private val httpClient =
        if(engine != null) HttpClient(engine) { applyDefaultConfiguration(modifiers) }
        else HttpClient { applyDefaultConfiguration(modifiers) }

    override suspend fun request(url: String, builder: HttpRequestBuilder.() -> Unit): HttpResponse {
        val request = HttpRequestBuilder().apply {
            url(url)
            builder()
        }

        val response = try {
            httpClient.request(builder)
        } catch(e: HttpRequestTimeoutException) {
            Napier.d { "Request timed out after $requestTimeout ms" }
            throw e
        } catch(e: Exception) {
            Napier.d { "Request failed with ${e.message}" }
            throw HttpRequestException(e.message ?: "", request)
        }

        if(logNetworkTraffic) {
            response.logResponse(url)
        }

        return response
    }

    suspend fun webSocketSession(url: String, block: HttpRequestBuilder.() -> Unit = {}) = httpClient.webSocketSession(url, block)

    fun close() = httpClient.close()

    private fun HttpResponse.logResponse(url: String) {
        Napier.d {
            """
                        
                        --------------------
                        Making a request to $url with method ${request.method.value}
                        Request headers: ${request.headers}
                        Request body: ${(request.content as? TextContent)?.text}
                        Response status: $status
                        Response headers: $headers
                        --------------------
                    """.trimIndent()
        }
    }

    private fun HttpClientConfig<*>.applyDefaultConfiguration(modifiers: List<HttpClientConfig<*>.() -> Unit>) {
        install(DefaultRequest) {
            headers {
                if(supabaseKey.isNotBlank()) {
                    append("apikey", supabaseKey)
                }
            }
            port = 443
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
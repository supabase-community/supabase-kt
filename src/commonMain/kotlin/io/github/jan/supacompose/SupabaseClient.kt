package io.github.jan.supacompose

import io.github.jan.supacompose.exceptions.RestException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed interface SupabaseClient {

    val supabaseUrl: String
    val supabaseKey: String
    val plugins: Map<String, Any>

    suspend fun makeRequest(method: HttpMethod, path: String, headers: Headers = Headers.build {  }, body: Any? = null): HttpResponse

}

internal class SupabaseClientImpl(
    override val supabaseUrl: String,
    override val supabaseKey: String,
    plugins: Map<String, (SupabaseClient) -> Any>,
) : SupabaseClient {

    private val httpClient = HttpClient {
        install(DefaultRequest) {
            headers {
                append("apikey", supabaseKey)
            }
            port = 443
        }
        install(ContentNegotiation) {
            json(supabaseJson)
        }
    }
    override val plugins = plugins.toList().associate { (key, value) ->
        key to value(this)
    }

    override suspend fun makeRequest(method: HttpMethod, path: String, headers: Headers, body: Any?) = httpClient.request {
        this.method = method
        url.takeFrom(supabaseUrl + path)
        headers {
            header("apikey", supabaseKey)
            headers.forEach { s, strings ->
                header(s, strings[0])
            }
        }
        setBody(body)
    }.also {
        if(it.status.value !in 200..299) {
            val error = it.body<JsonObject>()
            throw RestException(it.status.value, error["error"]?.jsonPrimitive?.content ?: "null", error["error_description"]?.jsonPrimitive?.content ?: "null")
        }
    }

}


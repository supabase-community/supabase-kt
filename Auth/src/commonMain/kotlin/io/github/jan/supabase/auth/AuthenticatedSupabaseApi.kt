@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")
package io.github.jan.supabase.auth

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.exception.SessionRequiredException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.network.SupabaseApi
import io.github.jan.supabase.network.SupabaseHttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement

typealias ResolveAccessToken = suspend (String?, Boolean) -> String?

@SupabaseInternal
data class AuthenticatedApiConfig(
    val jwtToken: String? = null,
    val defaultRequest: (HttpRequestBuilder.() -> Unit)? = null,
    val requireSession: Boolean,
    val getAccessToken: ResolveAccessToken
)

@OptIn(SupabaseInternal::class)
class AuthenticatedSupabaseApi @SupabaseInternal constructor(
    resolveUrl: (path: String) -> String,
    parseErrorResponse: (suspend (response: HttpResponse) -> RestException)? = null,
    httpClient: SupabaseHttpClient,
    val config: AuthenticatedApiConfig
): SupabaseApi(resolveUrl, parseErrorResponse, httpClient) {

    private val defaultRequest = config.defaultRequest
    private val jwtToken = config.jwtToken
    private val requireSession = config.requireSession

    override suspend fun rawRequest(url: String, builder: HttpRequestBuilder.() -> Unit): HttpResponse {
        val accessToken = config.getAccessToken(jwtToken, !requireSession)
            ?: throw SessionRequiredException(url)
        return super.rawRequest(url) {
            bearerAuth(accessToken)
            defaultRequest?.invoke(this)
            builder()
        }
    }

    suspend fun rawRequest(builder: HttpRequestBuilder.() -> Unit): HttpResponse = rawRequest("", builder)

    override suspend fun prepareRequest(
        url: String,
        builder: HttpRequestBuilder.() -> Unit
    ): HttpStatement {
        val accessToken = config.getAccessToken(jwtToken, !requireSession)
            ?: throw SessionRequiredException(url)
        return super.prepareRequest(url) {
            bearerAuth(accessToken)
            builder()
            defaultRequest?.invoke(this)
        }
    }

    companion object

}
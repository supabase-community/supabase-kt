package io.supabase.graphql

import com.apollographql.apollo.network.http.HttpInterceptor
import com.apollographql.apollo.network.http.HttpInterceptorChain
import io.ktor.http.HttpHeaders
import io.supabase.SupabaseClient
import io.supabase.auth.resolveAccessToken
import io.supabase.logging.d
import com.apollographql.apollo.api.http.HttpRequest as ApolloHttpRequest
import com.apollographql.apollo.api.http.HttpResponse as ApolloHttpResponse

internal class ApolloHttpInterceptor(private val supabaseClient: SupabaseClient, private val config: GraphQL.Config) : HttpInterceptor {

    override suspend fun intercept(
        request: ApolloHttpRequest,
        chain: HttpInterceptorChain
    ): ApolloHttpResponse {
        GraphQL.logger.d { "Intercepting Apollo request with url ${request.url}" }
        val accessToken = supabaseClient.resolveAccessToken(config.jwtToken) ?: error("Access token should not be null")
        val newRequest = request.newBuilder().apply {
            addHeader(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        return chain.proceed(newRequest.build())
    }

}

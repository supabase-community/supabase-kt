package io.github.jan.supabase.graphql

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.annotiations.SupabaseExperimental
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.plugins.MainConfig
import io.github.jan.supabase.plugins.MainPlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders

/**
 * Adds an apollo graphql client to supabase-kt with all necessary headers automatically managed.
 *
 *
 * This plugin uses the default GraphQL endpoint for supabase projects and adds the `apikey` and `Authorization` headers automatically
 */
sealed interface GraphQL: MainPlugin<GraphQL.Config> {

    /**
     * The apollo client. Customizable via [Config.apolloConfiguration]
     */
    val apolloClient: ApolloClient

    data class Config(
        override var customUrl: String? = null,
        override var jwtToken: String? = null,
        internal var apolloConfiguration: ApolloClient.Builder.() -> Unit = {}
    ): MainConfig {

        fun apolloConfiguration(configuration: ApolloClient.Builder.() -> Unit) {
            apolloConfiguration = configuration
        }

    }

    @SupabaseExperimental
    companion object: SupabasePluginProvider<Config, GraphQL> {

        override val key: String = "graphql"
        const val API_VERSION = 1

        override fun create(supabaseClient: SupabaseClient, config: Config): GraphQL {
            return GraphQLImpl(config, supabaseClient)
        }

        override fun createConfig(init: Config.() -> Unit): Config {
            return Config()
        }

        override fun setup(builder: SupabaseClientBuilder, config: Config) {
            builder.install(GraphQL)
        }


    }

}

@OptIn(SupabaseExperimental::class)
internal class GraphQLImpl(override val config: GraphQL.Config, override val supabaseClient: SupabaseClient) : GraphQL {

    override val API_VERSION: Int = GraphQL.API_VERSION
    override val PLUGIN_KEY: String = GraphQL.key
    override val apolloClient = ApolloClient.Builder().apply {
        serverUrl(config.customUrl ?: resolveUrl(""))
        addHttpHeader("apikey", supabaseClient.supabaseKey)
        addHttpInterceptor(ApolloHttpInterceptor())
        apply(config.apolloConfiguration)
    }.build()

    override suspend fun parseErrorResponse(response: HttpResponse): RestException {
        throw UnsupportedOperationException("Use apolloClient for graphql requests")
    }

    inner class ApolloHttpInterceptor: HttpInterceptor {
        override suspend fun intercept(
            request: HttpRequest,
            chain: HttpInterceptorChain
        ): com.apollographql.apollo3.api.http.HttpResponse {
            val accessToken = supabaseClient.pluginManager.getPluginOrNull(GoTrue)?.currentAccessTokenOrNull()
            val newRequest = request.newBuilder().apply {
                accessToken?.let {
                    addHeader(HttpHeaders.Authorization, "Bearer $it")
                }
            }
            return chain.proceed(newRequest.build())
        }

    }


}

@SupabaseExperimental
val SupabaseClient.graphql: GraphQL
    get() = pluginManager.getPlugin(GraphQL)
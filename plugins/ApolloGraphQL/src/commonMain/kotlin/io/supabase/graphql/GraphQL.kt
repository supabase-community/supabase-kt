package io.supabase.graphql

import com.apollographql.apollo.ApolloClient
import io.github.jan.supabase.BuildConfig
import io.supabase.SupabaseClient
import io.supabase.exceptions.RestException
import io.supabase.logging.SupabaseLogger
import io.supabase.plugins.MainConfig
import io.supabase.plugins.MainPlugin
import io.supabase.plugins.SupabasePluginProvider
import io.ktor.client.statement.HttpResponse

/**
 * Adds an Apollo GraphQL client to supabase-kt with all necessary headers automatically managed.
 *
 *
 * This plugin uses the default GraphQL endpoint for supabase projects and adds the `apikey` and `Authorization` headers automatically
 */
sealed interface GraphQL: MainPlugin<GraphQL.Config> {

    /**
     * The Apollo Client. Customizable via [Config.apolloConfiguration]
     */
    val apolloClient: ApolloClient

    /**
     * Config for the [GraphQL] plugin
     * @param apolloConfiguration custom apollo client configuration
     */
    data class Config(
        internal var apolloConfiguration: ApolloClient.Builder.() -> Unit = {}
    ): MainConfig() {

        /**
         * Add custom apollo client configuration
         */
        fun apolloConfiguration(configuration: ApolloClient.Builder.() -> Unit) {
            apolloConfiguration = configuration
        }

    }

    companion object: SupabasePluginProvider<Config, GraphQL> {

        override val key: String = "graphql"

        override val logger: SupabaseLogger = SupabaseClient.createLogger("Supabase-ApolloGraphQL")

        /**
         * The current graphql api version
         */
        const val API_VERSION = 1

        override fun create(supabaseClient: SupabaseClient, config: Config): GraphQL {
            return GraphQLImpl(config, supabaseClient)
        }

        override fun createConfig(init: Config.() -> Unit): Config {
            return Config().apply(init)
        }

    }

}

internal class GraphQLImpl(override val config: GraphQL.Config, override val supabaseClient: SupabaseClient) : GraphQL {

    override val apiVersion: Int = GraphQL.API_VERSION
    override val pluginKey: String = GraphQL.key
    override val apolloClient = ApolloClient.Builder().apply {
        serverUrl(config.customUrl ?: resolveUrl())
        addHttpHeader("apikey", supabaseClient.supabaseKey)
        addHttpHeader("X-Client-Info", "supabase-kt/${BuildConfig.PROJECT_VERSION}")
        addHttpInterceptor(ApolloHttpInterceptor(supabaseClient, config))
        apply(config.apolloConfiguration)
    }.build()

    override suspend fun parseErrorResponse(response: HttpResponse): RestException {
        throw UnsupportedOperationException("Use apolloClient for GraphQL requests")
    }


}

/**
 * With the [GraphQL] plugin installed, you can access a pre-made Apollo GraphQL client via this property
 */
val SupabaseClient.graphql: GraphQL
    get() = pluginManager.getPlugin(GraphQL)

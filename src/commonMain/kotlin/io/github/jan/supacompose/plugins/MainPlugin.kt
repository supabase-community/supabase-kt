package io.github.jan.supacompose.plugins

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.buildUrl
import io.ktor.http.appendEncodedPathSegments

/**
 * Config for [MainPlugin]s
 */
interface MainConfig {

    val customUrl: String?

}

/**
 * Represents main plugins like Auth or Functions
 */
interface MainPlugin <Config : MainConfig> : SupacomposePlugin {

    /**
     * The configuration for this plugin
     */
    val config: Config

    /**
     * The corresponding [SupabaseClient] instance
     */
    val supabaseClient: SupabaseClient

    /**
     * The version for the api the plugin is using
     */
    val API_VERSION: Int

    /**
     * The unique key for this plugin
     */
    val PLUGIN_KEY: String

    /**
     * Gets the auth url from either [config.customUrl] or [SupabaseClient.supabaseHttpUrl] and adds [path] to it
     */
    fun resolveUrl(path: String): String {
        val isBase = config.customUrl == null
        return buildUrl(config.customUrl ?: supabaseClient.supabaseHttpUrl) {
            if(isBase) {
                appendEncodedPathSegments(PLUGIN_KEY, "v${API_VERSION}")
            }
            appendEncodedPathSegments(path)
        }
    }

}
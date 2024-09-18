package io.github.jan.supabase.plugins

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.buildUrl
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.appendEncodedPathSegments

/**
 * Config for [MainPlugin]s
 */
open class MainConfig {

    /**
     * The url used for this module
     */
    var customUrl: String? = null

    /**
     * The jwt token used for this module. If null, the client will use the token from Auth's current session
     */
    var jwtToken: String? = null

}

/**
 * Represents main plugins like Auth or Functions
 */
interface MainPlugin <Config : MainConfig> : SupabasePlugin<Config> {

    /**
     * The version for the api the plugin is using
     */
    val apiVersion: Int

    /**
     * The unique key for this plugin
     */
    val pluginKey: String

    /**
     * Gets the auth url from either [MainConfig.customUrl] or [SupabaseClient.supabaseHttpUrl] and adds [path] to it
     */
    @OptIn(SupabaseInternal::class)
    fun resolveUrl(path: String = ""): String {
        val isBase = config.customUrl == null
        return buildUrl(config.customUrl ?: supabaseClient.supabaseHttpUrl) {
            if(isBase) {
                appendEncodedPathSegments(pluginKey, "v${apiVersion}")
            }
            if(path.isNotBlank()) {
                appendEncodedPathSegments(path)
            }
        }
    }

    /**
     * Parses the response from the server and builds a [RestException]
     */
    suspend fun parseErrorResponse(response: HttpResponse): RestException

}
package io.github.jan.supabase.auth.native.oauth

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OAuthConfig
import io.github.jan.supabase.auth.OAuthProvider
import io.github.jan.supabase.safeBody
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Links an OAuth Identity to an existing user.
 *
 * Example:
 * ```kotlin
 * supabase.auth.linkIdentity(OAuthProviders.Google)
 * ```
 *
 * This method works similar to [signInWithOAuth], see its documentation for more information.
 * @param provider The OAuth provider
 * @param config Extra configuration
 * @throws RestException or one of its subclasses if receiving an error response. If the error response contains an error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
 * @throws HttpRequestTimeoutException if the request timed out
 * @throws HttpRequestException on network related issues
 */
suspend fun Auth.linkIdentity(
    provider: OAuthProvider,
    config: OAuthConfig.() -> Unit = {}
) {
    val createdConfig = OAuthConfig().apply {
        redirectUrl = defaultRedirectUrl()
        config()
    }
    val fetchUrl: suspend (String?) -> String = { redirectTo: String? ->
        val url = getOAuthUrl(provider, "user/identities/authorize") {
            this.redirectUrl = redirectTo
            config()
        }
        val response = userApi.rawRequest(url) {
            method = HttpMethod.Get
            parameter("skip_http_redirect", true)
        }
        response.safeBody<JsonObject>()["url"]?.jsonPrimitive?.contentOrNull ?: error("No URL found in response")
    }
    startOAuthSession(
        createdConfig.redirectUrl,
        {
            fetchUrl(it)
        },
        { importSession(it) }
    )
}
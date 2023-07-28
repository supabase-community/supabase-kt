package io.github.jan.supabase.gotrue

import co.touchlab.kermit.Logger
import io.github.jan.supabase.gotrue.user.UserSession

internal fun noDeeplinkMessage(arg: String) = """
        Trying to generate a redirect url, but neither a fallback url was provided nor a deeplink $arg is set in the GoTrueConfig.
        If you want to use deep linking, set the scheme and host in the GoTrueConfig:
        install(GoTrue) {
            scheme = "YOUR_SCHEME"
            host = "YOUR_HOST"
        }
    """.trimIndent()

/**
 * Parses a session from a fragment.
 * @param fragment The fragment to parse the session from
 * @return The parsed session. Note that the user will be null, but you can retrieve it using [GoTrue.retrieveUser]
 */
fun GoTrue.parseSessionFromFragment(fragment: String): UserSession {
    val sessionParts = fragment.split("&").associate {
        it.split("=").let { pair ->
            pair[0] to pair[1]
        }
    }
    Logger.d { "Fragment parts: $sessionParts" }

    val accessToken = sessionParts["access_token"] ?: invalidArg("No access token found")
    val refreshToken = sessionParts["refresh_token"] ?: invalidArg("No refresh token found")
    val expiresIn = sessionParts["expires_in"]?.toLong() ?: invalidArg("No expires in found")
    val tokenType = sessionParts["token_type"] ?: invalidArg("No token type found")
    val type = sessionParts["type"] ?: ""
    val providerToken = sessionParts["provider_token"]
    val providerRefreshToken = sessionParts["provider_refresh_token"]
    return UserSession(
        accessToken = accessToken,
        refreshToken = refreshToken,
        providerRefreshToken = providerRefreshToken,
        providerToken = providerToken,
        expiresIn = expiresIn,
        tokenType = tokenType,
        user = null,
        type = type
    )
}

/**
 * Parses a session from an url.
 * @param url The url to parse the session from
 * @return The parsed session. Note that the user will be null, but you can retrieve it using [GoTrue.retrieveUser]
 */
fun GoTrue.parseSessionFromUrl(url: String): UserSession = parseSessionFromFragment(url.substringAfter("#"))
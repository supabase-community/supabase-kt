package io.github.jan.supabase.auth.native.native.url

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.buildUrl
import io.github.jan.supabase.logging.d

internal fun noDeeplinkError(arg: String): Nothing = error("""
        Trying to use a deeplink as a redirect url, but no deeplink $arg is set in the AuthConfig.
        If you want to use deep linking, set the scheme and host in the AuthConfig:
        install(Auth) {
            scheme = "YOUR_SCHEME"
            host = "YOUR_HOST"
        }
        You can also provide a custom redirect url.
    """.trimIndent())

/**
 * Parses a session from a fragment.
 * @param fragment The fragment to parse the session from
 * @return The parsed session. Note that the user will be null, but you can retrieve it using [getUser]
 */
fun Auth.parseSessionFromFragment(fragment: String): UserSession {
    val sessionParts = getFragmentParts(fragment)

    logger.d { "Fragment parts: $sessionParts" }

    val accessToken = requireNotNull(sessionParts["access_token"])
    val refreshToken = requireNotNull(sessionParts["refresh_token"])
    val expiresIn = requireNotNull(sessionParts["expires_in"]?.toLong())
    val tokenType = requireNotNull(sessionParts["token_type"])
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

internal fun Auth.validateHash(hash: String): UrlValidationResult {
    logger.d { "Parsing fragment/hash $hash" }
    val parameters = getFragmentParts(hash)
    if(handledUrlParameterError { parameters[it] }) {
        return UrlValidationResult.ErrorFound
    }
    val session = try {
        parseSessionFromFragment(hash)
    } catch(e: IllegalArgumentException) {
        logger.d(e) { "Received invalid session fragment. Ignoring." }
        return UrlValidationResult.Skipped
    }
    return UrlValidationResult.SessionFound(session)
}

internal fun getFragmentParts(fragment: String): Map<String, String> {
    val pairs = fragment.split("&")
    if(pairs.isEmpty()) return emptyMap()
    return pairs.mapNotNull {
        val keyAndValue = it.split("=")
        keyAndValue.getOrNull(0)?.let { key ->
            keyAndValue.getOrNull(1)?.let { value ->
                key to value
            }
        }
    }.toMap()
}

internal fun consumeHashParameters(parameters: List<String>, url: String): String {
    return buildUrl(url) {
        fragment = fragment.split("&").filter {
            it.split("=").first() !in parameters
        }.joinToString("&")
    }
}

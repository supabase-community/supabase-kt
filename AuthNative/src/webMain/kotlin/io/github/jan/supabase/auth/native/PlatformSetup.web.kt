package io.github.jan.supabase.auth.native

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.initDone
import io.github.jan.supabase.auth.native.url.UrlValidationResult
import io.github.jan.supabase.auth.native.url.consumeHashParameters
import io.github.jan.supabase.auth.native.url.consumeUrlParameter
import io.github.jan.supabase.auth.native.url.handledUrlParameterError
import io.github.jan.supabase.auth.native.url.validateHash
import io.github.jan.supabase.auth.tryToGetUser
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.logging.d
import io.github.jan.supabase.logging.w
import io.ktor.util.PlatformUtils.IS_BROWSER
import org.w3c.dom.url.URL

private val HASH_PARAMETERS = listOf(
    "access_token",
    "refresh_token",
    "expires_in",
    "expires_at",
    "token_type",
    "type",
    "provider_refresh_token",
    "provider_token",
    "error",
    "error_code",
    "error_description",
)

private val QUERY_PARAMETERS = listOf(
    "code",
    "error_code",
    "error",
    "error_description",
)

private fun cleanQueryParams(bridge: BrowserBridge) {
    val newURL = consumeUrlParameter(QUERY_PARAMETERS, bridge.href)
    bridge.replaceCurrentUrl(newURL);
}

private fun cleanHash(bridge: BrowserBridge) {
    val newURL = consumeHashParameters(HASH_PARAMETERS, bridge.href)
    bridge.replaceCurrentUrl(newURL)
}

private fun Auth.checkForHash(bridge: BrowserBridge): UserSession? {
    if(bridge.hash.isBlank()) return null
    val afterHash = bridge.hash.substring(1)
    if(!afterHash.contains('=')) return null
    logger.d { "Found hash: $afterHash" }
    return when(val result = validateHash(afterHash)) {
        is UrlValidationResult.SessionFound, UrlValidationResult.ErrorFound -> {
            cleanHash(bridge)
            (result as? UrlValidationResult.SessionFound)?.session
        }
        UrlValidationResult.Skipped -> null
    }
}

private fun Auth.checkForPKCECode(bridge: BrowserBridge): String? {
    val url = URL(bridge.href)
    if (handledUrlParameterError { url.searchParams.get(it) }) {
        cleanQueryParams(bridge)
        return null
    }
    val code = url.searchParams.get("code") ?: return null
    cleanQueryParams(bridge)
    return code
}

private suspend fun Auth.handlePKCECode(code: String) {
    logger.d { "Found PCKE code: $code" }
    try {
        val session = exchangeCodeForSession(code)
        importSession(session, source = SessionSource.External)
    } catch(e: Exception) {
        logger.w(e) { "Failed to exchange PCKE code for session" }
    }
    initDone()
}

private suspend fun Auth.handleHashSession(session: UserSession) {
    val user = tryToGetUser(session.accessToken)
    val newSession = session.copy(user = user)
    importSession(newSession, source = SessionSource.External)
}

internal actual suspend fun Auth.setupNativePlatform() {
    if(IS_BROWSER && !config.platformConfig().disableUrlChecking && config.platformConfig().browserBridge != null) {
        when(config.flowType) {
            FlowType.PKCE -> {
                logger.d { "Using PCKE flow type, checking for PCKE code..." }
                val code = checkForPKCECode(config.platformConfig().browserBridge!!) ?: return initDone()
                handlePKCECode(code)
            }
            FlowType.IMPLICIT -> {
                logger.d { "Using IMPLICIT flow type, checking for hash..." }
                val sessionInHash = checkForHash(config.platformConfig().browserBridge!!) ?: return initDone()
                handleHashSession(sessionInHash)
            }
        }
    } else {
        initDone()
    }
}
package io.github.jan.supabase.auth

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.logging.d
import io.github.jan.supabase.logging.e
import io.github.jan.supabase.logging.w
import io.ktor.util.PlatformUtils.IS_BROWSER
import kotlinx.coroutines.launch
import org.w3c.dom.url.URL

private fun cleanQueryParams(bridge: BrowserBridge) {
    val newURL = consumeUrlParameter(Auth.QUERY_PARAMETERS, bridge.href)
    bridge.replaceCurrentUrl(newURL);
}

private fun cleanHash(bridge: BrowserBridge) {
    val newURL = consumeHashParameters(Auth.HASH_PARAMETERS, bridge.href)
    //bridge.replaceCurrentUrl(newURL)
    Auth.logger.e { "hallo" }
}

private fun Auth.checkForHash(bridge: BrowserBridge): UserSession? {
    if(bridge.hash.isBlank()) return null
    val afterHash = bridge.hash.substring(1)
    if(!afterHash.contains('=')) return null
    Auth.logger.d { "Found hash: $afterHash" }
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

private fun Auth.handleHash(bridge: BrowserBridge) {
    if(bridge.hash.isBlank()) return initDone()
    val afterHash = bridge.hash.substring(1)

    if(!afterHash.contains('=')) {
        // No params after hash, no need to continue
        return initDone()
    }
    Auth.logger.d { "Found hash: $afterHash" }
    parseFragmentAndImportSession(afterHash) {
        val newURL = consumeHashParameters(Auth.HASH_PARAMETERS, bridge.href)
        bridge.replaceCurrentUrl(newURL);
        initDone()
    }
    return
}

private fun Auth.handlePKCECode(code: String) {
    this as AuthImpl
    Auth.logger.d { "Found PCKE code: $code" }
    authScope.launch {
        try {
            val session = exchangeCodeForSession(code)
            importSession(session, source = SessionSource.External)
        } catch(e: Exception) {
            Auth.logger.w(e) { "Failed to exchange PCKE code for session" }
        }
        initDone()
    }
}

private fun Auth.handleHashSession(session: UserSession) {
    Auth.logger.w { session.toString() }
    this as AuthImpl
    authScope.launch {
        val user = retrieveUser(session.accessToken) //TODO: Potentially catch any errors and still import the session then return false
        val newSession = session.copy(user = user)
        importSession(newSession, source = SessionSource.External)
    }
}

@SupabaseInternal
actual fun Auth.setupPlatform() {
    if(IS_BROWSER && !config.disableUrlChecking) {
        config.browserBridge?.let { bridge ->
            when(config.flowType) {
                FlowType.PKCE -> {
                    Auth.logger.d { "Using PCKE flow type, checking for PCKE code..." }
                    val code = checkForPKCECode(bridge) ?: return initDone()
                    handlePKCECode(code)
                }
                FlowType.IMPLICIT -> {
                    Auth.logger.d { "Using IMPLICIT flow type, checking for hash..." }
                    val sessionInHash = checkForHash(bridge) ?: return initDone()
                    handleHashSession(sessionInHash)
                    //Auth.logger.d { "Registering hash change listener..." }
                    /*bridge.onHashChange {
                        handleHash()
                    }*/
                }
            }
        }
    }
}
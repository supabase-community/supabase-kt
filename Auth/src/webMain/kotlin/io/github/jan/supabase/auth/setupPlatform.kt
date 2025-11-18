package io.github.jan.supabase.auth

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.logging.d
import io.github.jan.supabase.logging.w
import io.ktor.util.PlatformUtils.IS_BROWSER
import kotlinx.coroutines.launch
import org.w3c.dom.url.URL

@SupabaseInternal
actual fun Auth.setupPlatform() {
    this as AuthImpl

    fun checkForHash(bridge: BrowserBridge) {
        if(bridge.currentHash.isBlank()) return
        val afterHash = bridge.currentHash.substring(1)

        if(!afterHash.contains('=')) {
            // No params after hash, no need to continue
            return
        }
        Auth.logger.d { "Found hash: $afterHash" }
        parseFragmentAndImportSession(afterHash) {
            val newURL = consumeHashParameters(Auth.HASH_PARAMETERS, bridge.href)
            bridge.replaceCurrentUrl(newURL);
            initDone()
        }
    }

    fun checkForPCKECode(bridge: BrowserBridge) {
        val url = URL(bridge.href)
        var clean: Boolean
        if(handledUrlParameterError { url.searchParams.get(it) }) {
            clean = true
            initDone()
        } else {
            val code = url.searchParams.get("code") ?: return
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
            clean = true
        }
        if(clean) {
            val newURL = consumeUrlParameter(Auth.QUERY_PARAMETERS, bridge.href)
            bridge.replaceCurrentUrl(newURL);
        }
    }

    if(IS_BROWSER && !config.disableUrlChecking) {
        when(config.flowType) {
            FlowType.PKCE -> {
                Auth.logger.d { "Using PCKE flow type, checking for PCKE code..." }
                config.browserBridge?.let(::checkForPCKECode)
            }
            FlowType.IMPLICIT -> {
                Auth.logger.d { "Using IMPLICIT flow type, checking for hash..." }
                config.browserBridge?.let(::checkForHash)
                Auth.logger.d { "Registering hash change listener..." }
                config.browserBridge?.let {
                    it.onHashChange {
                        checkForHash(it)
                    }
                }
            }
        }
    }
}
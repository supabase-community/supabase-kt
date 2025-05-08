package io.github.jan.supabase.auth

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.logging.d
import io.ktor.util.PlatformUtils.IS_BROWSER
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.w3c.dom.url.URL

@SupabaseInternal
actual fun Auth.setupPlatform() {
    this as AuthImpl

    fun checkForHash() {
        if(window.location.hash.isBlank()) return

        val afterHash = window.location.hash.substring(1)

        if(!afterHash.contains('=')) {
            // No params after hash, no need to continue
            return
        }
        Auth.logger.d { "Found hash: $afterHash" }
        parseFragmentAndImportSession(afterHash) {
            val newURL = consumeHashParameters(Auth.HASH_PARAMETERS, window.location.href)
            window.history.replaceState(null, window.document.title, newURL);
        }
    }

    fun checkForPCKECode() {
        val url = URL(window.location.href)
        var clean: Boolean
        if(handledUrlParameterError { url.searchParams.get(it) }) {
            clean = true
        } else {
            val code = url.searchParams.get("code") ?: return
            Auth.logger.d { "Found PCKE code: $code" }
            authScope.launch {
                val session = exchangeCodeForSession(code)
                importSession(session, source = SessionSource.External)
            }
            clean = true
        }
        if(clean) {
            val newURL = consumeUrlParameter(Auth.QUERY_PARAMETERS, window.location.href)
            window.history.replaceState(null, window.document.title, newURL);
        }
    }

    if(IS_BROWSER) {
        Auth.logger.d { "Checking for hash..." }
        checkForHash()
        Auth.logger.d { "Checking for PCKE code..." }
        checkForPCKECode()
        Auth.logger.d { "Registering hash change listener..." }
        window.onhashchange = {
            checkForHash()
        }
    }
}
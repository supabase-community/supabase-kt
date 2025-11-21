package io.github.jan.supabase.auth.url

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.parseSessionFromFragment
import io.github.jan.supabase.buildUrl
import io.github.jan.supabase.logging.d

internal fun Auth.validateHash(hash: String): UrlValidationResult {
    Auth.logger.d { "Parsing fragment/hash $hash" }
    val parameters = getFragmentParts(hash)
    if(handledUrlParameterError { parameters[it] }) {
        return UrlValidationResult.ErrorFound
    }
    val session = try {
        parseSessionFromFragment(hash)
    } catch(e: IllegalArgumentException) {
        Auth.logger.d(e) { "Received invalid session fragment. Ignoring." }
        return UrlValidationResult.Skipped
    }
    return UrlValidationResult.SessionFound(session)
}

internal fun getFragmentParts(fragment: String) = fragment.split("&").associate {
    it.split("=").let { pair ->
        pair[0] to pair[1]
    }
}

internal fun consumeHashParameters(parameters: List<String>, url: String): String {
    return buildUrl(url) {
        fragment = fragment.split("&").filter {
            it.split("=").first() !in parameters
        }.joinToString("&")
    }
}

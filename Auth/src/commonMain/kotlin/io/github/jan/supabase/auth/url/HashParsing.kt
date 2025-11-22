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

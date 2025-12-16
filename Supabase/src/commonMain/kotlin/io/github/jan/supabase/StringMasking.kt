package io.github.jan.supabase

import io.ktor.http.Headers
import io.ktor.http.Url
import io.ktor.util.toMap

internal fun maskString(value: String, visibleCharacters: Int = 2, showLength: Boolean = false): String {
    if(value.isBlank()) return value;
    return value.take(visibleCharacters) + "..." + if(showLength) " (len=${value.length})" else ""
}

internal fun maskUrl(value: Url, visibleCharacters: Int = 2): String {
    return buildUrl(value) {
        host = "${host.take(visibleCharacters)}..."
    }
}

private val SENSITIVE_HEADERS = listOf("apikey", "Authorization")

internal fun maskHeaders(headers: Headers): String = headers.toMap().mapValues { (key, value) ->
    if(key in SENSITIVE_HEADERS) {
        value.firstOrNull()?.let {
            listOf(if(key == "Authorization") "Bearer ${maskString(it.drop(7), showLength = true)}" else maskString(it, showLength = true))
        }
    } else value
}.toString()

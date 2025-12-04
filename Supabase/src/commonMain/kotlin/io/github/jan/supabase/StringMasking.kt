package io.github.jan.supabase

import io.ktor.http.Url

internal fun maskString(value: String, visibleCharacters: Int = 2, showLength: Boolean = false): String {
    if(value.isBlank()) return value;
    return value.take(visibleCharacters) + "..." + if(showLength) " (len=${value.length})" else ""
}

internal fun maskUrl(value: Url, visibleCharacters: Int = 2): String {
    return buildUrl(value) {
        host = "${host.take(visibleCharacters)}..."
    }
}
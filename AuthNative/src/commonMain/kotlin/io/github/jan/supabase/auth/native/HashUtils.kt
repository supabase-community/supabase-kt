package io.github.jan.supabase.auth.native

import okio.ByteString.Companion.toByteString

internal fun String.hash(): String {
    val hash = this.encodeToByteArray().toByteString()
    return hash.sha256().hex()
}
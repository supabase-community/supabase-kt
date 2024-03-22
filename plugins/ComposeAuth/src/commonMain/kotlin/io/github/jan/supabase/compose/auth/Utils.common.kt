package io.github.jan.supabase.compose.auth

import io.ktor.utils.io.core.toByteArray
import korlibs.crypto.SHA256

internal fun String.hash(): String {
    val hash = SHA256.digest(this.toByteArray())
    return hash.hex
}
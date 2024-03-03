package io.github.jan.supabase.compose.auth

import io.ktor.util.Digest
import io.ktor.utils.io.core.toByteArray

@OptIn(ExperimentalStdlibApi::class)
internal suspend fun String.hash(): String {
    val digest = Digest("SHA-256")
    digest += this.toByteArray()
    return digest.build().toHexString()
}
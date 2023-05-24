package io.github.jan.supabase.gotrue

import korlibs.crypto.SHA256
import korlibs.crypto.SecureRandom
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
internal fun generateCodeVerifier(): String {
    val bytes = ByteArray(64)
    SecureRandom.nextBytes(bytes)
    return Base64.UrlSafe.encode(bytes)
}

@OptIn(ExperimentalEncodingApi::class)
internal fun generateCodeChallenge(codeVerifier: String): String {
    val bytes = codeVerifier.encodeToByteArray()
    val hash = SHA256.digest(bytes)
    return Base64.UrlSafe.encode(hash.bytes).replace("=", "")
}
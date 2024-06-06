@file:Suppress("MatchingDeclarationName")
package io.github.jan.supabase.gotrue

import korlibs.crypto.SHA256
import korlibs.crypto.SecureRandom
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal object PKCEConstants {
    const val VERIFIER_LENGTH = 64
    const val CHALLENGE_METHOD = "s256"
}

@OptIn(ExperimentalEncodingApi::class)
internal fun generateCodeVerifier(): String {
    val bytes = ByteArray(PKCEConstants.VERIFIER_LENGTH)
    SecureRandom.nextBytes(bytes)
    return Base64.UrlSafe.encode(bytes)
}

@OptIn(ExperimentalEncodingApi::class)
internal fun generateCodeChallenge(codeVerifier: String): String {
    val bytes = codeVerifier.encodeToByteArray()
    val hash = SHA256.digest(bytes)
    return Base64.UrlSafe.encode(hash.bytes).replace("=", "")
}
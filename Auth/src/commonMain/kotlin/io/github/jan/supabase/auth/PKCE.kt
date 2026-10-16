@file:Suppress("MatchingDeclarationName")
package io.github.jan.supabase.auth

import dev.whyoleg.cryptography.random.CryptographyRandom
import io.github.jan.supabase.buildUrl
import okio.ByteString.Companion.toByteString
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal object PKCEConstants {
    const val VERIFIER_LENGTH = 64
    const val CHALLENGE_METHOD = "s256"
    const val PKCE_FLOW_ID_PARAM = "sb_flow_id"
    const val PKCE_MAX_CONCURRENT_FLOWS = 5
    val PKCE_FLOW_ID_PATTERN = Regex("^[a-zA-Z0-9_-]{8,64}")
}

internal fun validatePKCEFlowId(flowId: String?): String? {
    return if(flowId != null && PKCEConstants.PKCE_FLOW_ID_PATTERN.matches(flowId)) flowId else null
}

internal fun Auth.appendFlowIdIfEnabled(redirectUrl: String, flowId: String?): String {
    return if(flowId == null || !config.appendPkceFlowIdToRedirects) redirectUrl else buildUrl(redirectUrl) {
        parameters[PKCEConstants.PKCE_FLOW_ID_PARAM] = flowId
    }
}

internal fun generatePKCEFlowId(): String {
    val bytes = ByteArray(16)
    CryptographyRandom.nextBytes(bytes)
    return bytes.toHexString()
}

internal fun pkceVerifierSlotKey(storageKey: String, flowId: String) = "${storageKey}-flow-${flowId}-code-verifier"

internal fun pkceFlowIndexKey(storageKey: String) = "${storageKey}-flows-code-verifier"

internal fun pkceLegacyKey(storageKey: String) = "${storageKey}-code-verifier"

@OptIn(ExperimentalEncodingApi::class)
internal fun generateCodeVerifier(): String {
    val bytes = ByteArray(PKCEConstants.VERIFIER_LENGTH)
    CryptographyRandom.nextBytes(bytes)
    return Base64.UrlSafe.encode(bytes)
}

@OptIn(ExperimentalEncodingApi::class)
internal fun generateCodeChallenge(codeVerifier: String): String {
    val byteString = codeVerifier.encodeToByteArray().toByteString()
    val hash = byteString.sha256()
    return Base64.UrlSafe.encode(hash.toByteArray()).replace("=", "")
}
package io.github.jan.supabase.auth.passkey

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/**
 * Response for [AuthPasskeyApi.startRegistration]
 * @param challengeId The challenge id
 * @param options The server options
 * @param expiresAt When the registration expires at
 */
data class PasskeyRegistrationResponse(
    @SerialName("challenge_id") val challengeId: String,
    val options: JsonObject,
    @SerialName("expires_at") val expiresAt: Instant
)

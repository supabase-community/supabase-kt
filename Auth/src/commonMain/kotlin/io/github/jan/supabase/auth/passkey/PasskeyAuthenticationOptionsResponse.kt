package io.github.jan.supabase.auth.passkey

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

data class PasskeyAuthenticationOptionsResponse(
    @SerialName("challenge_id") val challengeId: String,
    val options: JsonObject,
    @SerialName("expires_at") val expiresAt: Instant
)

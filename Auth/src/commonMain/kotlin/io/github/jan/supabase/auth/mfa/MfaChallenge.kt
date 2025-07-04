package io.github.jan.supabase.auth.mfa

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * A challenge to verify the user's identity.
 * @property id The id of the challenge.
 * @property factorType Factor Type which generated the challenge.
 */
@Serializable
data class MfaChallenge(val id: String, @SerialName("type") val factorType: String) {

    @SerialName("expires_at") private val expiresAtSeconds: Long = 0

    /**
     * Timestamp in UNIX seconds when this challenge will no longer be usable.
     */
    val expiresAt: Instant
        get() = Instant.fromEpochSeconds(expiresAtSeconds)

}

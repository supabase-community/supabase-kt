package io.github.jan.supabase.gotrue.mfa

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A challenge to verify the user's identity.
 * @property id The id of the challenge.
 */
@Serializable
data class MfaChallenge(val id: String) {

    @SerialName("expires_at") private val expiresAtSeconds: Long = 0

    /**
     * The time when the challenge expires.
     */
    val expiresAt: Instant
        get() = Instant.fromEpochSeconds(expiresAtSeconds)

}

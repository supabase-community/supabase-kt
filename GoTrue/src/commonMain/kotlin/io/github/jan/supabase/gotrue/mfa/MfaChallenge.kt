package io.github.jan.supabase.gotrue.mfa

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MfaChallenge(val id: String) {

    @SerialName("expires_at") private val expiresAtSeconds: Long = 0
    val expiresAt: Instant
        get() = Instant.fromEpochSeconds(expiresAtSeconds)

}

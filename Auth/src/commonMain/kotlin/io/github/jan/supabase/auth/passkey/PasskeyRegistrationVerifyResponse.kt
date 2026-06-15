package io.github.jan.supabase.auth.passkey

import kotlinx.serialization.SerialName
import kotlin.time.Instant

/**
 * Response for [AuthPasskeyApi.verifyRegistration]
 * @param id The uuid of the passkey
 * @param friendlyName The friendly name of the passkey
 * @param createdAt When the passkey was created at
 */
data class PasskeyRegistrationVerifyResponse(
    val id: String,
    @SerialName("friendly_name") val friendlyName: String? = null,
    @SerialName("created_at") val createdAt: Instant
)

package io.github.jan.supabase.auth.passkey

import kotlinx.serialization.SerialName
import kotlin.time.Instant

/**
 * A passkey item for [AuthPasskeyApi.list]
 * @param id The uuid for the passkey
 * @param friendlyName The friendly name of the passkey
 * @param createdAt When the passkey was created at
 * @param lastUsedAt When the passkey was last used at
 */
data class PasskeyListItem(
    val id: String,
    @SerialName("friendly_name") val friendlyName: String? = null,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("last_used_at") val lastUsedAt: Instant? = null
)

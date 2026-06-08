package io.github.jan.supabase.auth.passkey

import kotlinx.serialization.SerialName
import kotlin.time.Instant

data class PasskeyMetadata(
    val id: String,
    @SerialName("friendly_name") val friendlyName: String? = null,
    @SerialName("created_at") val createdAt: Instant
)

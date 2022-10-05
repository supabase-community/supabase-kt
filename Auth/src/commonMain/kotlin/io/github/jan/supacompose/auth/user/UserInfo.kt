package io.github.jan.supacompose.auth.user

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class UserInfo(
    @SerialName("app_metadata")
    val appMetadata: AppMetadata,
    @SerialName("aud")
    val aud: String,
    @SerialName("confirmation_sent_at")
    val confirmationSentAt: Instant? = null,
    @SerialName("confirmed_at")
    val confirmedAt: Instant? = null,
    @SerialName("created_at")
    val createdAt: Instant? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("email_confirmed_at")
    val emailConfirmedAt: Instant? = null,
    @SerialName("id")
    val id: String,
    @SerialName("identities")
    val identities: List<Identity>? = null,
    @SerialName("last_sign_in_at")
    val lastSignInAt: Instant? = null,
    @SerialName("phone")
    val phone: String? = null,
    @SerialName("role")
    val role: String? = null,
    @SerialName("updated_at")
    val updatedAt: Instant? = null,
    @SerialName("user_metadata")
    val userMetadata: Map<String, JsonPrimitive> = emptyMap(),
    @SerialName("phone_change_sent_at")
    val phoneChangeSentAt: Instant? = null,
    @SerialName("new_phone")
    val newPhone: String? = null,
    @SerialName("email_change_sent_at")
    val emailChangeSentAt: Instant? = null,
    @SerialName("new_email")
    val newEmail: String? = null,
    @SerialName("invited_at")
    val invitedAt: Instant? = null,
    @SerialName("recovery_sent_at")
    val recoverySentAt: Instant? = null,
    @SerialName("phone_confirmed_at")
    val phoneConfirmedAt: Instant? = null,
    @SerialName("action_link")
    val actionLink: String? = null,
)
@file:UseSerializers(DateTimeSerializer::class)
package io.github.jan.supacompose.auth.user

import io.github.jan.supacompose.auth.serializers.DateTimeSerializer
import com.soywiz.klock.DateTimeTz
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class UserInfo(
    @SerialName("app_metadata")
    val appMetadata: AppMetadata,
    @SerialName("aud")
    val aud: String,
    @SerialName("confirmation_sent_at")
    val confirmationSentAt: DateTimeTz? = null,
    @SerialName("confirmed_at")
    val confirmedAt: DateTimeTz? = null,
    @SerialName("created_at")
    val createdAt: DateTimeTz? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("email_confirmed_at")
    val emailConfirmedAt: DateTimeTz? = null,
    @SerialName("id")
    val id: String,
    @SerialName("identities")
    val identities: List<Identity> = emptyList(),
    @SerialName("last_sign_in_at")
    val lastSignInAt: DateTimeTz? = null,
    @SerialName("phone")
    val phone: String? = null,
    @SerialName("role")
    val role: String? = null,
    @SerialName("updated_at")
    val updatedAt: DateTimeTz? = null,
    @SerialName("user_metadata")
    val userMetadata: Map<String, JsonPrimitive> = emptyMap(),
    @SerialName("phone_change_sent_at")
    val phoneChangeSentAt: DateTimeTz? = null,
    @SerialName("new_phone")
    val newPhone: String? = null,
    @SerialName("email_change_sent_at")
    val emailChangeSentAt: DateTimeTz? = null,
    @SerialName("new_email")
    val newEmail: String? = null,
    @SerialName("invited_at")
    val invitedAt: DateTimeTz? = null,
    @SerialName("recovery_sent_at")
    val recoverySentAt: DateTimeTz? = null,
    @SerialName("phone_confirmed_at")
    val phoneConfirmedAt: DateTimeTz? = null,
    @SerialName("action_link")
    val actionLink: String? = null,
)
package io.github.jan.supacompose.auth.admin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class UserUpdateBuilder(
    var email: String? = null,
    var password: String? = null,
    @SerialName("app_metadata")
    var appMetadata: JsonObject? = null,
    @SerialName("user_metadata")
    var userMetadata: JsonObject? = null,
    @SerialName("email_confirm")
    var emailConfirm: Boolean? = null,
    @SerialName("phone_confirm")
    var phoneConfirm: Boolean? = null,
    var phone: String? = null
)

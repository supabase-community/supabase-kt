package io.github.jan.supabase.gotrue.settings

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class Settings(
    @SerialName("disable_signup") val disableSignup: Boolean,
    @SerialName("external") val externalProviders: External,
    @SerialName("external_labels") val externalLabels: ExternalLabels,
    @SerialName("mailer_autoconfirm") val mailerAutoconfirm: Boolean,
    @SerialName("phone_autoconfirm") val phoneAutoconfirm: Boolean,
    @SerialName("sms_provider") val smsProvider: String
)
package io.github.jan.supabase.gotrue.mfa

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.supabaseJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

/**
 * Represents an MFA factor type
 * @param value The name of the factor type
 */
sealed class FactorType<T>(val value: String) {

    @SupabaseInternal
    abstract suspend fun decodeResponse(json: JsonObject): T

    /**
     * TOTP (timed one-time password) MFA factor
     */
    object TOTP : FactorType<TOTP.Response>("totp") {

        override suspend fun decodeResponse(json: JsonObject): Response {
            return supabaseJson.decodeFromJsonElement(json["totp"]?.jsonObject ?: error("No 'totp' object found in factor response"))
        }

        /**
         * @param secret The secret used to generate the TOTP code
         * @param qrCode The QR code used to enroll the TOTP factor in a svg format
         * @param uri The URI used to enroll the TOTP factor
         */
        @Serializable
        data class Response(
            val secret: String,
            @SerialName("qr_code")
            val qrCode: String,
            val uri: String
        )

    }

}
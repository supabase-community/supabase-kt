package io.github.jan.supabase.gotrue.mfa

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.supabaseJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Represents an MFA factor type
 * @param value The name of the factor type
 */
sealed class FactorType<Config, Response>(val value: String) {

    @SupabaseInternal
    abstract suspend fun encodeConfig(config: Config.() -> Unit): JsonObject

    @SupabaseInternal
    abstract suspend fun decodeResponse(json: JsonObject): Response

    /**
     * TOTP (timed one-time password) MFA factor
     */
    data object TOTP : FactorType<TOTP.Config, TOTP.Response>("totp") {

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

        /**
         * @param issuer Domain which the user is enrolling with
         */
        @Serializable
        data class Config(
            var issuer: String? = null,
        )

        override suspend fun decodeResponse(json: JsonObject): Response {
            return supabaseJson.decodeFromJsonElement(json["totp"]?.jsonObject ?: error("No 'totp' object found in factor response"))
        }

        override suspend fun encodeConfig(config: Config.() -> Unit): JsonObject {
            return supabaseJson.encodeToJsonElement(Config().apply(config)).jsonObject
        }

    }

    /**
     * TOTP (timed one-time password) MFA factor
     */
    data object SMS : FactorType<SMS.Config, SMS.Response>("sms") {

        /**

         */
        @Serializable
        data class Response(
            @SerialName("phone_number")
            val phone: String
        )

        /**
         * @param phone The phone number to send the SMS to
         */
        @Serializable
        data class Config(
            @SerialName("phone_number")
            var phone: String? = null,
        )

        override suspend fun decodeResponse(json: JsonObject): Response {
            return Response(json["phone_number"]?.jsonPrimitive?.contentOrNull ?: error("No 'phone_number' entry found in factor response"))
        }

        override suspend fun encodeConfig(config: Config.() -> Unit): JsonObject {
            return supabaseJson.encodeToJsonElement(Config().apply(config)).jsonObject
        }

    }

}
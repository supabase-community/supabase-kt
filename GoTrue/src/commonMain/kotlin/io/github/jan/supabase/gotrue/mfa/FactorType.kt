package io.github.jan.supabase.gotrue.mfa

import io.github.jan.supabase.supabaseJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

sealed class FactorType<T>(val value: String) {

    abstract suspend fun decodeResponse(json: JsonObject): T

    object TOTP : FactorType<TOTP.Response>("totp") {

        override suspend fun decodeResponse(json: JsonObject): Response {
            return supabaseJson.decodeFromJsonElement(json["totp"]?.jsonObject ?: throw IllegalStateException("No 'totp' object found in factor response"))
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
package io.github.jan.supabase.gotrue.admin

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable(with = UserBuilder.Companion::class)
sealed class UserBuilder {

    var userMetadata: JsonObject? = null
    var appMetadata: JsonObject? = null

    /**
     * Automatically confirms either the phone number or the email address
     */
    var autoConfirm: Boolean = false

    /**
     * The user's password
     */
    var password: String = ""

    /**
     * Adds user-specific metadata
     */
    fun userMetadata(metadata: JsonObjectBuilder.() -> Unit) {
        userMetadata = buildJsonObject(metadata)
    }

    /**
     * Adds app-specific metadata
     */
    fun appMetadata(metadata: JsonObjectBuilder.() -> Unit) {
        appMetadata = buildJsonObject(metadata)
    }

    /**
     * @property email The user's email address
     */
    data class Email(var email: String = "") : UserBuilder()

    /**
     * @property phoneNumber The user's phone number
     */
    data class Phone(var phoneNumber: String = "") : UserBuilder()

    companion object : KSerializer<UserBuilder> {

        override val descriptor = buildClassSerialDescriptor("io.github.jan.supabase.gotrue.admin.UserBuilder") {
            element("password", String.serializer().descriptor)
            element("email", String.serializer().descriptor, isOptional = true)
            element("email_confirm", Boolean.serializer().descriptor, isOptional = true)
            element("phone", String.serializer().descriptor, isOptional = true)
            element("phone_confirm", Boolean.serializer().descriptor, isOptional = true)
            element("user_metadata", JsonObject.serializer().descriptor, isOptional = true)
        }

        override fun deserialize(decoder: Decoder): UserBuilder {
            throw IllegalStateException("This serializer is only used for serialization")
        }

        override fun serialize(encoder: Encoder, value: UserBuilder) {
            encoder as JsonEncoder
            if(value.password.isBlank()) throw IllegalArgumentException("Password must not be blank")
            if(value is Email && value.email.isBlank()) throw IllegalArgumentException("Email must not be blank")
            if(value is Phone && value.phoneNumber.isBlank()) throw IllegalArgumentException("Phone number must not be blank")
            encoder.encodeJsonElement(buildJsonObject {
                put("password", value.password)
                value.userMetadata?.let { put("user_metadata", it) }
                value.appMetadata?.let { put("app_metadata", it) }
                when(value) {
                    is Email -> {
                        put("email", value.email)
                        put("email_confirm", value.autoConfirm)
                    }
                    is Phone -> {
                        put("phone", value.phoneNumber)
                        put("phone_confirm", value.autoConfirm)
                    }
                }
            })

        }
    }

}
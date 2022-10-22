package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.checkErrors
import io.github.jan.supabase.gotrue.generateRedirectUrl
import io.github.jan.supabase.gotrue.providers.AuthProvider
import io.github.jan.supabase.gotrue.user.UserSession
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

sealed interface DefaultAuthProvider<C, R> : AuthProvider<C, R> {

    @Serializable(with = DefaultAuthProvider.Config.Companion::class)
    sealed class Config(
        var password: String = "",
        var captchaToken: String? = null
    ) {

        companion object  : KSerializer<Config> {

            override val descriptor = buildClassSerialDescriptor("io.github.jan.supabase.gotrue.providers.builtin.Config") {
                element("email", String.serializer().descriptor, isOptional = true)
                element("password", String.serializer().descriptor)
                element("phone", String.serializer().descriptor, isOptional = true)
                element("gotrue_meta_security", JsonObject.serializer().descriptor, isOptional = true)
            }

            override fun serialize(encoder: Encoder, value: Config) {
                encoder as JsonEncoder
                encoder.encodeJsonElement(buildJsonObject {
                    when(value) {
                        is Email.Config -> put("email", value.email)
                        is Phone.Config -> put("phone", value.phoneNumber)
                    }
                    put("password", value.password)
                    value.captchaToken?.let {
                        putJsonObject("gotrue_meta_security") {
                            put("captcha_token", value.captchaToken)
                        }
                    }
                })
            }

            override fun deserialize(decoder: Decoder): Config {
                throw NotImplementedError()
            }

        }
    }

    override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (C.() -> Unit)?
    ) {
        if(config == null) throw IllegalArgumentException("Credentials are required")
        val encodedCredentials = encodeCredentials(config)
        val response = supabaseClient.httpClient.post(supabaseClient.gotrue.resolveUrl("token?grant_type=password")) {
            setBody(encodedCredentials)
        }
        response.checkErrors()
        response.body<UserSession>().also {
            onSuccess(it)
        }
    }

    override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (C.() -> Unit)?
    ): R {
        if (config == null) throw IllegalArgumentException("Credentials are required")
        val finalRedirectUrl = supabaseClient.gotrue.generateRedirectUrl(redirectUrl)
        val redirect = finalRedirectUrl?.let {
            "?redirect_to=$finalRedirectUrl"
        } ?: ""
        val body = encodeCredentials(config)
        val response = supabaseClient.httpClient.post(supabaseClient.gotrue.resolveUrl("signup$redirect")) {
            setBody(body)
        }
        response.checkErrors()
        val json = response.body<JsonObject>()
        return decodeResult(json)
    }

    fun decodeResult(json: JsonObject): R

    fun encodeCredentials(credentials: C.() -> Unit): String

}
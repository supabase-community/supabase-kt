package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.GoTrueImpl
import io.github.jan.supabase.gotrue.generateRedirectUrl
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.AuthProvider
import io.github.jan.supabase.gotrue.redirectTo
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.supabaseJson
import io.ktor.client.call.body
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

sealed interface DefaultAuthProvider<C, R> : AuthProvider<C, R> {

    @Serializable
    sealed class Config(
        var captchaToken: String? = null
    ) {

        companion object : KSerializer<Config> {

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
                        is Email.Config -> {
                            put("email", value.email)
                            put("password", value.password)
                        }
                        is Phone.Config -> {
                            put("phone", value.phoneNumber)
                            put("password", value.password)
                        }
                        is IDToken.Config -> {
                            put("id_token", value.id_token)
                            put("client_id", value.client_id)
                            put("provider", value.provider)
                            value.nonce?.let {
                                put("nonce", it)
                            }
                        }
                        else -> throw IllegalArgumentException("Unknown config type")
                    }
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
        val finalRedirectUrl = supabaseClient.gotrue.generateRedirectUrl(redirectUrl)
        val gotrue = supabaseClient.gotrue as GoTrueImpl
        val url = "token?grant_type=${
            when (this) {
                Email -> "password"
                Phone -> "password"
                IDToken -> "id_token"
            }
        }"
        val response = gotrue.api.post(url, encodedCredentials) {
            finalRedirectUrl?.let { redirectTo(it) }
        }
        response.body<UserSession>().also {
            onSuccess(it)
        }
    }

    override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (C.() -> Unit)?
    ): R? {
        if (config == null) throw IllegalArgumentException("Credentials are required")
        val finalRedirectUrl = supabaseClient.gotrue.generateRedirectUrl(redirectUrl)
        val body = encodeCredentials(config)
        val gotrue = supabaseClient.gotrue as GoTrueImpl
        val url = when (this) {
            Email -> "signup"
            Phone -> "signup"
            IDToken -> "token?grant_type=id_token"
        }
        val response = gotrue.api.post(url, body) {
            finalRedirectUrl?.let { redirectTo(it) }
        }
        val json = response.body<JsonObject>()
        if(json.containsKey("access_token")) {
            val userSession = supabaseJson.decodeFromJsonElement<UserSession>(json)
            onSuccess(userSession)
            return null
        }
        return decodeResult(json)
    }

    fun decodeResult(json: JsonObject): R

    fun encodeCredentials(credentials: C.() -> Unit): String

}
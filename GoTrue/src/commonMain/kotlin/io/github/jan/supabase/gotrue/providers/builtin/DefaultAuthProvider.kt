package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.gotrue.FlowType
import io.github.jan.supabase.gotrue.AuthImpl
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.generateCodeChallenge
import io.github.jan.supabase.gotrue.generateCodeVerifier
import io.github.jan.supabase.gotrue.generateRedirectUrl
import io.github.jan.supabase.gotrue.providers.AuthProvider
import io.github.jan.supabase.gotrue.redirectTo
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.putJsonObject
import io.github.jan.supabase.supabaseJson
import io.ktor.client.call.body
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put

/**
 * A default authentication provider
 * @see Email
 * @see Phone
 * @see IDToken
 */
sealed interface DefaultAuthProvider<C, R> : AuthProvider<C, R> {

    /**
     * The grant type of the provider.
     */
    val grantType: String

    /**
     * The default configuration for the provider.
     * @param captchaToken The captcha token when having captcha enabled
     * @param data Extra data for the user
     */
    @Serializable
    sealed class Config(
        @Serializable(with = CaptchaTokenSerializer::class)
        @SerialName("gotrue_meta_security")
        var captchaToken: String? = null,
        var data: JsonObject? = null,
    )

    override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (C.() -> Unit)?
    ) {
        require(config != null) { "Credentials are required" }
        val encodedCredentials = encodeCredentials(config)
        val finalRedirectUrl = supabaseClient.auth.generateRedirectUrl(redirectUrl)
        val gotrue = supabaseClient.auth as AuthImpl
        val url = "token?grant_type=$grantType"
        val response = gotrue.api.postJson(url, encodedCredentials) {
            finalRedirectUrl?.let { redirectTo(it) }
        }
        response.body<UserSession>().also {
            onSuccess(it)
        }
    }

    @OptIn(SupabaseExperimental::class)
    override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (C.() -> Unit)?
    ): R? {
        require(config != null) { "Credentials are required" }
        val finalRedirectUrl = supabaseClient.auth.generateRedirectUrl(redirectUrl)
        val body = encodeCredentials(config)
        val gotrue = supabaseClient.auth as AuthImpl
        var codeChallenge: String? = null
        if(gotrue.config.flowType == FlowType.PKCE) {
            val codeVerifier = generateCodeVerifier()
            gotrue.codeVerifierCache.saveCodeVerifier(codeVerifier)
            codeChallenge = generateCodeChallenge(codeVerifier)
        }
        val url = when (this) {
            Email -> "signup"
            Phone -> "signup"
            IDToken -> "token?grant_type=id_token"
        }
        val response = gotrue.api.postJson(url, buildJsonObject {
            putJsonObject(body)
            codeChallenge?.let {
                put("code_challenge", it)
                put("code_challenge_method", "s256")
            }
        }) {
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

    @SupabaseInternal
    fun decodeResult(json: JsonObject): R

    @SupabaseInternal
    fun encodeCredentials(credentials: C.() -> Unit): JsonObject

}
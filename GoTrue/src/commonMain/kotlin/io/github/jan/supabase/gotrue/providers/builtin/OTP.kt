package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.encodeToJsonElement
import io.github.jan.supabase.gotrue.AuthImpl
import io.github.jan.supabase.gotrue.FlowType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.generateCodeChallenge
import io.github.jan.supabase.gotrue.generateCodeVerifier
import io.github.jan.supabase.gotrue.generateRedirectUrl
import io.github.jan.supabase.gotrue.providers.AuthProvider
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.putJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * One Time Password (OTP) [AuthProvider] for Supabase.
 *
 * You need to provide either an email or a phone number.
 */
data object OTP: AuthProvider<OTP.Config, Unit> {

    /**
     * The configuration for the OTP authentication method
     *
     * Note: Only [email] or [phoneNumber] can be set
     *
     * @param email The email of the user
     * @param phoneNumber The phone number of the user
     * @param data Additional data to store with the user
     * @param createUser Whether to create a new user if the user doesn't exist
     *
     */
    class Config(
        @PublishedApi internal val serializer: SupabaseSerializer,
        var email: String? = null,
        var phoneNumber: String? = null,
        var data: JsonObject? = null,
        var createUser: Boolean = false,
    ) {

        inline fun <reified T : Any> data(data: T) {
            this.data = serializer.encodeToJsonElement(data) as JsonObject
        }

    }

    override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (Config.() -> Unit)?
    ) {
        val otpConfig = Config(supabaseClient.auth.serializer).apply(config ?: {})
        require((otpConfig.email != null && otpConfig.email!!.isNotBlank()) || (otpConfig.phoneNumber != null && otpConfig.phoneNumber!!.isNotBlank())) { "You need to provide either an email or a phone number" }
        val finalRedirectUrl = supabaseClient.auth.generateRedirectUrl(redirectUrl)
        val body = buildJsonObject {
            put("create_user", otpConfig.createUser)
            otpConfig.data?.let {
                put("data", it)
            }
            otpConfig.email?.let {
                put("email", it)
            } ?: otpConfig.phoneNumber?.let {
                put("phone", it)
            }
        }
        var codeChallenge: String? = null
        if (supabaseClient.auth.config.flowType == FlowType.PKCE) {
            val codeVerifier = generateCodeVerifier()
            supabaseClient.auth.codeVerifierCache.saveCodeVerifier(codeVerifier)
            codeChallenge = generateCodeChallenge(codeVerifier)
        }
        (supabaseClient.auth as AuthImpl).api.postJson("otp", buildJsonObject {
            putJsonObject(body)
            codeChallenge?.let {
                put("code_challenge", it)
                put("code_challenge_method", "s256")
            }
        }) {
            finalRedirectUrl?.let { url.parameters.append("redirect_to", it) }
        }
    }

    override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (Config.() -> Unit)?
    ): Unit? = login(supabaseClient, onSuccess, redirectUrl, config)

}
package io.supabase.auth.providers.builtin

import io.supabase.SupabaseClient
import io.supabase.SupabaseSerializer
import io.supabase.auth.AuthImpl
import io.supabase.auth.FlowType
import io.supabase.auth.auth
import io.supabase.auth.generateCodeChallenge
import io.supabase.auth.generateCodeVerifier
import io.supabase.auth.providers.AuthProvider
import io.supabase.auth.putCaptchaToken
import io.supabase.auth.user.UserSession
import io.supabase.encodeToJsonElement
import io.supabase.putJsonObject
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
     * Note: Only [email] or [phone] can be set
     *
     * @param email The email of the user
     * @param phone The phone number of the user
     * @param data Additional data to store with the user
     * @param createUser Whether to create a new user if the user doesn't exist
     * @param captchaToken The captcha token for the request
     */
    class Config(
        @PublishedApi internal val serializer: SupabaseSerializer,
        var email: String? = null,
        var phone: String? = null,
        var data: JsonObject? = null,
        var createUser: Boolean = true,
        var captchaToken: String? = null
    ) {

        /**
         * Sets [data] to the given value.
         *
         * Encoded using [Auth.serializer]
         */
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

        require((otpConfig.email != null && otpConfig.email!!.isNotBlank()) || (otpConfig.phone != null && otpConfig.phone!!.isNotBlank())) { "You need to provide either an email or a phone number" }
        require(!(otpConfig.email != null && otpConfig.phone != null)) { "You can only provide either an email or a phone number" }

        val body = buildJsonObject {
            put("create_user", otpConfig.createUser)
            otpConfig.data?.let {
                put("data", it)
            }
            otpConfig.email?.let {
                put("email", it)
            } ?: otpConfig.phone?.let {
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
            otpConfig.captchaToken?.let { putCaptchaToken(it) }
        }) {
            redirectUrl?.let { url.parameters.append("redirect_to", it) }
        }
    }

    override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (Config.() -> Unit)?
    ): Unit = login(supabaseClient, onSuccess, redirectUrl, config)

}
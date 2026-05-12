package io.github.jan.supabase.auth.passkey

import io.github.jan.supabase.auth.api.AuthenticatedSupabaseApi
import io.github.jan.supabase.safeBody
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

interface AuthPasskeyApi {

    /**
     * Start passkey registration for the current authenticated user.
     * Returns WebAuthn credential creation options to pass to navigator.credentials.create().
     */
    suspend fun startRegistration(): PasskeyRegistrationOptions

    /**
     * Verify passkey registration with the credential response.
     * The credentialResponse should be the serialized output of navigator.credentials.create().
     */
    suspend fun verifyRegistration(challengeId: String, credential: String): PasskeyMetadata

    /**
     * Start passkey authentication.
     * Returns WebAuthn credential request options to pass to navigator.credentials.get().
     */
    suspend fun startAuthentication(builder: PasskeyAuthenticationBuilder.() -> Unit = {}): PasskeyAuthenticationOptionsResponse

    /**
     * Verify passkey authentication and create a session.
     * The credential should be the serialized output of navigator.credentials.get().
     */
    suspend fun verifyAuthentication(challengeId: String, credential: String): PasskeyAuthenticationVerifyResponse

    /**
     * List all passkeys for the current user.
     */
    suspend fun list(): List<PasskeyListItem>

    /**
     * Update a passkey.
     */
    suspend fun delete(passkeyId: String)

    /**
     * Delete a passkey.
     */
    suspend fun update(passkeyId: String, friendlyName: String): PasskeyListItem

}

internal class AuthPasskeyApiImpl(
    private val api: AuthenticatedSupabaseApi
): AuthPasskeyApi {

    override suspend fun startRegistration(): PasskeyRegistrationOptions {
        val result = api.post("registration/options")
        return result.safeBody()
    }

    override suspend fun verifyRegistration(
        challengeId: String,
        credential: String
    ): PasskeyMetadata {
        return api.postJson("registration/verify", buildJsonObject {
            put("challenge_id", challengeId)
            put("credential", Json.decodeFromString(credential))
        }).safeBody()
    }

    override suspend fun startAuthentication(builder: PasskeyAuthenticationBuilder.() -> Unit): PasskeyAuthenticationOptionsResponse {
        return api.postJson("authentication/options", buildJsonObject {
            val builder = PasskeyAuthenticationBuilder().apply(builder)
            builder.captchaToken?.let {
                putJsonObject("gotrue_meta_security") {
                    put("captcha_token", it)
                }
            }
        }).safeBody()
    }

    override suspend fun verifyAuthentication(
        challengeId: String,
        credential: String
    ): PasskeyAuthenticationVerifyResponse {
        return api.postJson("authentication/verify", buildJsonObject {
            put("challenge_id", challengeId)
            put("credential", Json.decodeFromString(credential))
        }).safeBody()
    }

    override suspend fun list(): List<PasskeyListItem> {
        return api.get("").safeBody()
    }

    override suspend fun delete(passkeyId: String) {
        api.delete(passkeyId)
    }

    override suspend fun update(
        passkeyId: String,
        friendlyName: String
    ): PasskeyListItem {
        return api.patchJson(passkeyId, buildJsonObject {
            put("friendly_name", friendlyName)
        }).safeBody()
    }


}
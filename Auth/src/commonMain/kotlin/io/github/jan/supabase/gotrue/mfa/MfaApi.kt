package io.github.jan.supabase.gotrue.mfa

import io.github.jan.supabase.gotrue.AuthImpl
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.user.UserMfaFactor
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.safeBody
import io.ktor.client.call.body
import io.ktor.util.decodeBase64String
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * An interface for interacting with Multi-Factor Authentication Api in Supabase Auth.
 */
sealed interface MfaApi {

    /**
     * Checks whether the current session was authenticated with MFA or the user has a verified MFA factor.
     */
    val isMfaEnabledFlow: Flow<Boolean>

    /**
     * Checks whether the user has a verified MFA factor.
     */
    val isMfaEnabled: Boolean
        get() {
            val mfaLevel = getAuthenticatorAssuranceLevel()
            return mfaLevel.next == AuthenticatorAssuranceLevel.AAL2
        }

    /**
     * Returns all verified factors from the current user session. If the user has no verified factors or there is no session, an empty list is returned.
     */
    val verifiedFactors: List<UserMfaFactor>

    /**
     * Checks whether the current session is authenticated with MFA
     */
    val loggedInUsingMfa: Boolean
        get() = getAuthenticatorAssuranceLevel().current == AuthenticatorAssuranceLevel.AAL2

    /**
     * Checks whether the current session is authenticated with MFA
     */
    val loggedInUsingMfaFlow: Flow<Boolean>

    /**
     * @param factorType The type of MFA factor to enroll. Currently only supports TOTP.
     * @param issuer Domain which the user is enrolling with
     * @param friendlyName Human readable name assigned to a device
     * @return MfaEnrollResponse containing the id of the MFA factor, the type of MFA factor and the data of the MFA factor (like QR-Code for TOTP)
     * @see FactorType
     */
    suspend fun <Response> enroll(factorType: FactorType<Response>, issuer: String? = null, friendlyName: String? = null): MfaFactor<Response>

    /**
     * Unenrolls an MFA factor
     * @param factorId The id of the MFA factor to unenroll
     */
    suspend fun unenroll(factorId: String)

    /**
     * Creates a new MFA challenge, which can be used to verify the user's code using [verifyChallenge]
     */
    suspend fun createChallenge(factorId: String): MfaChallenge

    /**
     * Creates a new MFA challenge and immediately verifies it
     * @param factorId The id of the MFA factor to verify
     * @param code The code to verify
     * @param saveSession Whether to save the session after verification in Supabase Auth
     */
    suspend fun createChallengeAndVerify(factorId: String, code: String, saveSession: Boolean = true): UserSession {
        val challenge = createChallenge(factorId)
        return verifyChallenge(factorId, challenge.id, code, saveSession)
    }

    /**
     * Verifies a MFA challenge
     * @param factorId The id of the MFA factor to verify
     * @param challengeId The id of the challenge to verify
     * @param code The code to verify
     * @param saveSession Whether to save the session after verification in Supabase Auth
     */
    suspend fun verifyChallenge(factorId: String, challengeId: String, code: String, saveSession: Boolean = true): UserSession

    /**
     * Retrieves all factors for the current user
     */
    suspend fun retrieveFactorsForCurrentUser(): List<UserMfaFactor>

    /**
     * Parses the current JWT and returns the AuthenticatorAssuranceLevel for the current session and the next session
     */
    fun getAuthenticatorAssuranceLevel(): MfaLevel

}

internal class MfaApiImpl(
    val auth: AuthImpl
) : MfaApi {

    override val isMfaEnabledFlow: Flow<Boolean> = auth.sessionStatus.map {
        when(it) {
            is SessionStatus.Authenticated -> isMfaEnabled
            SessionStatus.LoadingFromStorage -> false
            SessionStatus.NetworkError -> false
            is SessionStatus.NotAuthenticated -> false
        }
    }
    override val loggedInUsingMfaFlow: Flow<Boolean> = auth.sessionStatus.map {
        when(it) {
            is SessionStatus.Authenticated -> loggedInUsingMfa
            SessionStatus.LoadingFromStorage -> false
            SessionStatus.NetworkError -> false
            is SessionStatus.NotAuthenticated -> false
        }
    }
    override val verifiedFactors: List<UserMfaFactor>
        get() = (auth.sessionStatus.value as? SessionStatus.Authenticated)?.session?.user?.factors?.filter(UserMfaFactor::isVerified) ?: emptyList()

    val api = auth.api

    override suspend fun <Response> enroll(
        factorType: FactorType<Response>,
        issuer: String?,
        friendlyName: String?
    ): MfaFactor<Response> {
        val result = api.postJson("factors", buildJsonObject {
            put("factor_type", factorType.value)
            issuer?.let { put("issuer", it) }
            friendlyName?.let { put("friendly_name", it) }
        })
        val json = result.body<JsonObject>()
        val factorData = factorType.decodeResponse(json)
        return MfaFactor(
            json["id"]!!.jsonPrimitive.content,
            factorType.value,
            factorData
        )
    }


    override suspend fun createChallenge(factorId: String): MfaChallenge {
        val result = api.post("factors/$factorId/challenge")
        return result.safeBody()
    }

    override suspend fun verifyChallenge(
        factorId: String,
        challengeId: String,
        code: String,
        saveSession: Boolean
    ): UserSession {
        val result = api.postJson("factors/$factorId/verify", buildJsonObject {
            put("code", code)
            put("challenge_id", challengeId)
        })
        val session = result.body<UserSession>()
        if(saveSession) {
            auth.importSession(session)
        }
        return session
    }

    override suspend fun unenroll(factorId: String) {
        api.delete("factors/$factorId")
    }

    override fun getAuthenticatorAssuranceLevel(): MfaLevel {
        val jwt = auth.currentAccessTokenOrNull() ?: error("Current session is null")
        val parts = jwt.split(".")
        val decodedJwt = Json.decodeFromString<JsonObject>(parts[1].decodeBase64String())
        val aal = AuthenticatorAssuranceLevel.from(decodedJwt["aal"]?.jsonPrimitive?.content ?: error("No 'aal' claim found in JWT"))
        val nextAal = if (verifiedFactors.isNotEmpty()) AuthenticatorAssuranceLevel.AAL2 else AuthenticatorAssuranceLevel.AAL1
        return MfaLevel(aal, nextAal)
    }


    override suspend fun retrieveFactorsForCurrentUser(): List<UserMfaFactor> {
        return auth.retrieveUser(auth.currentAccessTokenOrNull() ?: error("Current session is null")).factors
    }

}
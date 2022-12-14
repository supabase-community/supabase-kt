package io.github.jan.supabase.gotrue.mfa

import io.github.jan.supabase.gotrue.GoTrueImpl
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.user.UserSession
import io.ktor.client.call.body
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okio.ByteString.Companion.decodeBase64

sealed interface MfaApi {

    /**
     * Returns the current mfa status of the user as a flow (changes whenever the user session changes)
     */
    val isMfaEnabledFlow: Flow<Boolean>

    /**
     * Checks whether the current session was authenticated with MFA or the user has a verified MFA factor.
     */
    val isMfaEnabled: Boolean
        get() {
            val mfaLevel = getAuthenticatorAssuranceLevel()
            return mfaLevel.current == AuthenticatorAssuranceLevel.AAL2 || mfaLevel.next == AuthenticatorAssuranceLevel.AAL2
        }

    /**
     * @param factorType The type of MFA factor to enroll. Currently only supports TOTP. Defaults to TOTP.
     * @param issuer Domain which the user is enrolling with
     * @param friendlyName Human readable name assigned to a device
     * @return MfaEnrollResponse containing the id of the MFA factor, the type of MFA factor and the data of the MFA factor (like QR-Code for TOTP)
     * @see FactorType
     */
    suspend fun <Response> enroll(factorType: FactorType<Response>, issuer: String? = null, friendlyName: String? = null): MfaEnroll<Response>

    suspend fun unenroll(factorId: String)

    suspend fun createChallenge(factorId: String): MfaChallenge

    suspend fun verifyChallenge(factorId: String, challengeId: String, code: String): UserSession

    suspend fun verifyChallengeAndLogin(factorId: String, challengeId: String, code: String)

    fun getAuthenticatorAssuranceLevel(): MfaLevel

}

internal class MfaApiImpl(
    val gotrue: GoTrueImpl
) : MfaApi {

    override val isMfaEnabledFlow: Flow<Boolean> = gotrue.sessionStatus.map {
        when(it) {
            is SessionStatus.Authenticated -> isMfaEnabled
            SessionStatus.LoadingFromStorage -> false
            SessionStatus.NetworkError -> false
            SessionStatus.NotAuthenticated -> false
        }
    }
    val api = gotrue.api

    override suspend fun <Response> enroll(
        factorType: FactorType<Response>,
        issuer: String?,
        friendlyName: String?
    ): MfaEnroll<Response> {
        val result = api.postJson("factors", buildJsonObject {
            put("factor_type", factorType.value)
            issuer?.let { put("issuer", it) }
            friendlyName?.let { put("friendly_name", it) }
        })
        val json = result.body<JsonObject>()
        val factorData = factorType.decodeResponse(json)
        return MfaEnroll(
            json["id"]!!.jsonPrimitive.content,
            factorType.value,
            factorData
        )
    }


    override suspend fun createChallenge(factorId: String): MfaChallenge {
        val result = api.post("factors/$factorId/challenge")
        return result.body()
    }

    override suspend fun verifyChallenge(factorId: String, challengeId: String, code: String): UserSession {
        val result = api.postJson("factors/$factorId/verify", buildJsonObject {
            put("code", code)
            put("challenge_id", challengeId)
        })
        return result.body()
    }

    override suspend fun verifyChallengeAndLogin(factorId: String, challengeId: String, code: String) {
        val session = verifyChallenge(factorId, challengeId, code)
        gotrue.importSession(session)
    }

    override suspend fun unenroll(factorId: String) {
        api.delete("factors/$factorId")
    }

    override fun getAuthenticatorAssuranceLevel(): MfaLevel {
        val jwt = gotrue.currentAccessTokenOrNull() ?: throw IllegalStateException("Current session is null")
        val parts = jwt.split(".")
        val decodedJwt = Json.decodeFromString<JsonObject>(parts[1].decodeBase64()?.utf8() ?: throw IllegalStateException("Could not decode current JWT"))
        val aal = AuthenticatorAssuranceLevel.from(decodedJwt["aal"]?.jsonPrimitive?.content ?: throw IllegalStateException("No 'aal' claim found in JWT"))
        val nextAal = AuthenticatorAssuranceLevel.AAL1 //TODO
        return MfaLevel(aal, nextAal)
    }

}
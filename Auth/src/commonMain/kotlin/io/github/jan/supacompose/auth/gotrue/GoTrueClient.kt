package io.github.jan.supacompose.auth.gotrue

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.providers.AuthProvider
import io.github.jan.supacompose.auth.providers.DefaultAuthProvider
import io.github.jan.supacompose.auth.providers.AuthFail
import io.github.jan.supacompose.auth.settings.Settings
import io.github.jan.supacompose.auth.user.UserInfo
import io.github.jan.supacompose.auth.user.UserSession
import io.github.jan.supacompose.putJsonObject
import io.github.jan.supacompose.supabaseJson
import io.github.jan.supacompose.toJsonObject
import io.ktor.client.call.body
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

sealed interface GoTrueClient {

    /**
     * Signs up a new user with the specified [provider]
     */
    suspend fun <C, R, Provider : AuthProvider<C, R>> signUpWith(
        provider: Provider,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (AuthFail) -> Unit = {},
        config: (C.() -> Unit)? = null
    ): R

    /**
     * Logins the user with the specified [provider]
     */
    suspend fun <C, R, Provider : AuthProvider<C, R>> loginWith(
        provider: Provider,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (AuthFail) -> Unit,
        config: (C.() -> Unit)?
    )

    /**
     * Modifies a user with the specified [provider]. Extra data can be supplied
     * @param provider The provider to use
     * @param config The configuration to use
     * @param jwt The JWT to use
     */
    suspend fun <C, R, Provider : DefaultAuthProvider<C, R>> modifyUser(
        provider: Provider,
        jwt: String,
        config: C.() -> Unit = {}
    ) = modifyUser(provider, jwt, kotlinx.serialization.json.JsonNull, config)

    /**
     * Gets all publicly available information about the gotrue instance
     */
    suspend fun settings(): Settings

    /**
     * Sends a one time password to the specified [provider]
     */
    suspend fun <C, R, Provider : DefaultAuthProvider<C, R>> sendOtpTo(provider: Provider, createUser: Boolean = false, config: C.() -> Unit)

    /**
     * Sends a password reset email to the user with the specified [email]
     */
    suspend fun sendRecoveryEmail(email: String)

    /**
     * Sends a nonce to the user's email (preferred) or phone
     */
    suspend fun reauthenticate(jwt: String)

    /**
     * Revokes all refresh tokens for the user
     */
    suspend fun logout(jwt: String)

    /**
     * Refreshes a session
     */
    suspend fun refreshSession(refreshToken: String): UserSession

    /**
     * Verifies a registration, invite or a password recovery
     */
    suspend fun verify(type: VerifyType, token: String): UserSession

    /**
     * Gets the user's information from the session
     */
    suspend fun getUser(jwt: String): UserInfo

    companion object {

        fun create(supabaseClient: SupabaseClient): GoTrueClient = GoTrueClientImpl(supabaseClient)

    }

}

@PublishedApi
internal class GoTrueClientImpl(val supabaseClient: SupabaseClient): GoTrueClient {

    override suspend fun settings(): Settings = supabaseJson.decodeFromString(supabaseClient.makeRequest(HttpMethod.Get, "/auth/v1/settings").body())

    override suspend fun <C, R, Provider : AuthProvider<C, R>> signUpWith(
        provider: Provider,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (AuthFail) -> Unit,
        config: (C.() -> Unit)?
    ): R = provider.signUp(supabaseClient, onSuccess, onFail, config)

    override suspend fun <C, R, Provider : AuthProvider<C, R>> loginWith(
        provider: Provider,
        onSuccess: suspend (UserSession) -> Unit,
        onFail: (AuthFail) -> Unit,
        config: (C.() -> Unit)?
    ) = provider.login(supabaseClient, onSuccess, onFail, config)

    override suspend fun <C, R, Provider : DefaultAuthProvider<C, R>> sendOtpTo(
        provider: Provider,
        createUser: Boolean,
        config: C.() -> Unit
    ) {
        val body = buildJsonObject {
            putJsonObject(provider.encodeCredentials(config).toJsonObject())
            put("create_user", createUser)
        }.toString()
        supabaseClient.makeRequest(HttpMethod.Post, "/auth/v1/otp", body = body)
    }

    override suspend fun sendRecoveryEmail(email: String) {
        val body = buildJsonObject {
            put("email", email)
        }.toString()
        supabaseClient.makeRequest(HttpMethod.Post, "/auth/v1/recovery", body = body)
    }

    override suspend fun logout(jwt: String) {
        supabaseClient.makeRequest(HttpMethod.Post, "/auth/v1/logout", Headers.build {
            append("Authorization", "Bearer $jwt")
        })
    }

    override suspend fun reauthenticate(jwt: String) {
        supabaseClient.makeRequest(HttpMethod.Get, "/auth/v1/reauthenticate", Headers.build {
            append("Authorization", "Bearer $jwt")
        })
    }

    override suspend fun refreshSession(refreshToken: String): UserSession {
        val body = """
            {
              "refresh_token": "$refreshToken"
            }
        """.trimIndent()
        val response = supabaseClient.makeRequest(HttpMethod.Post, "/auth/v1/token?grant_type=refresh_token", body = body)
        return supabaseJson.decodeFromString(response.body())
    }

    override suspend fun verify(type: VerifyType, token: String): UserSession {
        val body = """
            {
              "type": "${type.name.lowercase()}",
              "token": "$token"
            }
        """.trimIndent()
        val response = supabaseClient.makeRequest(HttpMethod.Post, "/auth/v1/verify", body = body)
        return supabaseJson.decodeFromString(response.body())
    }

    override suspend fun getUser(jwt: String): UserInfo {
        val response = supabaseClient.makeRequest(HttpMethod.Get, "/auth/v1/user", Headers.build {
            append("Authorization", "Bearer $jwt")
        })
        return supabaseJson.decodeFromString(response.body())
    }

}

/**
 * Modifies a user with the specified [provider]. Extra data can be supplied
 * @param provider The provider to use
 * @param config The configuration to use
 * @param extraData The extra data to use
 * @param jwt The JWT to use
 */
suspend inline fun <C, R, reified D, Provider : DefaultAuthProvider<C, R>> GoTrueClient.modifyUser(
    provider: Provider,
    jwt: String,
    extraData: D?,
    noinline config: C.() -> Unit = {}
): UserInfo {
    val body = if(extraData != JsonNull) {
        buildJsonObject {
            putJsonObject(provider.encodeCredentials(config).toJsonObject())
            put("data", supabaseJson.encodeToJsonElement(extraData))
        }.toString()
    } else {
        provider.encodeCredentials(config)
    }
    val response = (this as GoTrueClientImpl).supabaseClient.makeRequest(HttpMethod.Put, "/auth/v1/user", Headers.build {
        append("Authorization", "Bearer $jwt")
    }, body)
    return supabaseJson.decodeFromString(response.body<String>().also(::println))
}
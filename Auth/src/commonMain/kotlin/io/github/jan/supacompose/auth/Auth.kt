package io.github.jan.supacompose.auth

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.annotiations.SupaComposeInternal
import io.github.jan.supacompose.auth.providers.AuthFail
import io.github.jan.supacompose.auth.providers.AuthProvider
import io.github.jan.supacompose.auth.providers.DefaultAuthProvider
import io.github.jan.supacompose.auth.user.UserInfo
import io.github.jan.supacompose.auth.user.UserSession
import io.github.jan.supacompose.plugins.SupabasePlugin
import io.github.jan.supacompose.putJsonObject
import io.github.jan.supacompose.supabaseJson
import io.github.jan.supacompose.toJsonObject
import io.ktor.client.call.body
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement

sealed interface Auth {

    val currentSession: StateFlow<UserSession?>
    val supabaseClient: SupabaseClient
    val sessionManager: SessionManager

    /**
     * Signs up a new user with the specified [provider]
     */
    suspend fun <C, R, Provider : AuthProvider<C, R>> signUpWith(
        provider: Provider,
        redirectUrl: String? = null,
        config: (C.() -> Unit)? = null
    ): R

    /**
     * Logins the user with the specified [provider]
     */
    suspend fun <C, R, Provider : AuthProvider<C, R>> loginWith(
        provider: Provider,
        redirectUrl: String? = null,
        config: (C.() -> Unit)? = null
    )

    /**
     * Modifies a user with the specified [provider]. Extra data can be supplied
     * @param provider The provider to use
     * @param config The configuration to use
     */
    suspend fun <C, R, Provider : DefaultAuthProvider<C, R>> modifyUser(
        provider: Provider,
        config: C.() -> Unit = {}
    ): UserInfo

    /**
     * Sends a one time password to the specified [provider]
     */
    suspend fun <C, R, Provider : DefaultAuthProvider<C, R>> sendOtpTo(provider: Provider, createUser: Boolean = false, redirectUrl: String? = null, config: C.() -> Unit)

    /**
     * Sends a password reset email to the user with the specified [email]
     */
    suspend fun sendRecoveryEmail(email: String, redirectUrl: String? = null)

    /**
     * Sends a nonce to the user's email (preferred) or phone
     */
    suspend fun reauthenticate()

    /**
     * Revokes all refresh tokens for the user
     */
    suspend fun logout()

    /**
     * Verifies a registration, invite or password recovery
     */
    suspend fun verify(type: VerifyType, token: String)

    /**
     * Retrieves the current user with the session
     */
    suspend fun getUser(jwt: String): UserInfo

    class Config(val params: MutableMap<String, Any> = mutableMapOf(), var sessionSaving: Boolean = true)

    companion object : SupabasePlugin<Config, Auth> {

        override val key = "auth"
        const val API_VERSION = 1

        override fun create(supabaseClient: SupabaseClient, config: Config.() -> Unit): Auth = AuthImpl(supabaseClient, Config().apply(config))

    }

}

/**
 * Modifies a user with the specified [provider]. Extra data can be supplied
 * @param provider The provider to use
 * @param config The configuration to use
 * @param extraData The extra data to use
 * @param jwt The JWT to use
 */
suspend inline fun <C, R, reified D, Provider : DefaultAuthProvider<C, R>> Auth.modifyUser(
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
    val response = supabaseClient.makeRequest(HttpMethod.Put, "/auth/v${Auth.API_VERSION}/user", Headers.build {
        append("Authorization", "Bearer $jwt")
    }, body)
    return response.body()
}

enum class VerifyType {
    SIGNUP,
    RECOVERY,
    INVITE
}

val SupabaseClient.auth: Auth
    get() = plugins.getOrElse("auth") {
        throw IllegalStateException("Auth plugin not installed")
    } as? Auth ?: throw IllegalStateException("Auth plugin not installed")
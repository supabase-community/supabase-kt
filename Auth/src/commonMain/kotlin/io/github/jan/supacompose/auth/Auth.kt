package io.github.jan.supacompose.auth

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.providers.AuthProvider
import io.github.jan.supacompose.auth.providers.DefaultAuthProvider
import io.github.jan.supacompose.auth.user.UserInfo
import io.github.jan.supacompose.auth.user.UserSession
import io.github.jan.supacompose.exceptions.RestException
import io.github.jan.supacompose.plugins.MainConfig
import io.github.jan.supacompose.plugins.MainPlugin
import io.github.jan.supacompose.plugins.SupacomposePlugin
import io.github.jan.supacompose.plugins.SupacomposePluginProvider
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed interface Auth : MainPlugin<Auth.Config> {

    /**
     * Returns the current user session as a [StateFlow]
     */
    val currentSession: StateFlow<UserSession?>

    /**
     * Whether the [currentSession] session is getting refreshed automatically
     */
    val isAutoRefreshRunning: Boolean

    /**
     * Returns the session manager instance
     */
    val sessionManager: SessionManager

    /**
     * Returns the auth's status to distinguish between [Status.LOADING_FROM_STORAGE] in and [Status.NOT_AUTHENTICATED] when dealing with [currentSession] being null
     */
    val status: StateFlow<Status>

    /**
     * Signs up a new user with the specified [provider]
     * @param provider the provider to use for signing up
     * @param redirectUrl The redirect url to use. If you don't specify this, the platform specific will be use, like deeplinks on android.
     * @param config The configuration to use for the sign-up.
     * @throws RestException If the credentials are invalid
     */
    suspend fun <C, R, Provider : AuthProvider<C, R>> signUpWith(
        provider: Provider,
        redirectUrl: String? = null,
        config: (C.() -> Unit)? = null
    ): R

    /**
     * Logins the user with the specified [provider]
     * @param provider the provider to use for signing up
     * @param redirectUrl The redirect url to use. If you don't specify this, the platform specific will be use, like deeplinks on android.
     * @param config The configuration to use for the sign-up.
     * @throws RestException If the credentials are invalid
     */
    suspend fun <C, R, Provider : AuthProvider<C, R>> loginWith(
        provider: Provider,
        redirectUrl: String? = null,
        config: (C.() -> Unit)? = null
    )

    /**
     * Modifies a user with the specified [provider]. Extra data can be supplied
     * @param provider The provider to use
     * @param extraData Extra data to store
     * @param config The configuration to use
     * @throws RestException If the current session is invalid
     */
    suspend fun <C, R, Provider : DefaultAuthProvider<C, R>> modifyUser(
        provider: Provider,
        extraData: JsonObject? = null,
        config: C.() -> Unit = {}
    ): UserInfo

    /**
     * Sends a one time password to the specified [provider]
     * @param provider The provider to use
     * @param createUser Whether to create a user when a user with the given credentials doesn't exist
     * @param redirectUrl The redirect url to use. If you don't specify this, the platform specific will be use, like deeplinks on android.
     */
    suspend fun <C, R, Provider : DefaultAuthProvider<C, R>> sendOtpTo(provider: Provider, createUser: Boolean = false, redirectUrl: String? = null, config: C.() -> Unit)

    /**
     * Sends a password reset email to the user with the specified [email]
     * @param email The email to send the password reset email to
     * @param redirectUrl The redirect url to use. If you don't specify this, the platform specific will be use, like deeplinks on android.
     */
    suspend fun sendRecoveryEmail(email: String, redirectUrl: String? = null)

    /**
     * Sends a nonce to the user's email (preferred) or phone
     */
    suspend fun reauthenticate()

    /**
     * Revokes all refresh tokens for the user, and invalidates the session
     */
    suspend fun invalidateAllRefreshTokens()

    /**
     * Verifies a registration, invite or password recovery
     * @param type The type of the verification
     * @param token The token used to verify
     */
    suspend fun verify(type: VerifyType, token: String)

    /**
     * Retrieves the current user with the session
     */
    suspend fun getUser(jwt: String): UserInfo

    /**
     * Registers a callback to be called when the user session changes
     */
    fun onSessionChange(callback: (new: UserSession?, old: UserSession?) -> Unit)

    /**
     * Invalidates the current session, which means [currentSession] will be null
     */
    suspend fun invalidateSession()

    /**
     * Imports a user session and starts auto-refreshing if [autoRefresh] is true
     */
    suspend fun importSession(session: UserSession, autoRefresh: Boolean = true)

    /**
     * Imports the jwt token and retrieves the user profile.
     * Be aware auto-refreshing is not available when importing **only** a jwt token.
     */
    suspend fun importAuthToken(jwt: String) = importSession(UserSession(jwt, "", 0L, "", getUser(jwt)), false)

    data class Config(val params: MutableMap<String, Any> = mutableMapOf(), var retryDelay: Duration = 10.seconds, var alwaysAutoRefresh: Boolean = true, override var customUrl: String? = null): MainConfig

    enum class Status {
        LOADING_FROM_STORAGE,
        NETWORK_ERROR,
        AUTHENTICATED,
        NOT_AUTHENTICATED
    }

    companion object : SupacomposePluginProvider<Config, Auth> {

        override val key = "auth"
        const val API_VERSION = 1

        override fun createConfig(init: Config.() -> Unit) = Config().apply(init)
        override fun create(supabaseClient: SupabaseClient, config: Config): Auth = AuthImpl(supabaseClient, config)

    }

}

/**
 * Modifies a user with the specified [provider]. Extra data can be supplied
 * @param provider The provider to use
 * @param config The configuration to use
 * @param extraData The extra data to use
 * @throws RestException If the current session is invalid
 */
suspend inline fun <C, R, reified D, Provider : DefaultAuthProvider<C, R>> Auth.modifyUser(
    provider: Provider,
    extraData: D? = null,
    noinline config: C.() -> Unit = { }
): UserInfo = modifyUser(provider, extraData?.let { Json.encodeToJsonElement(extraData) }?.jsonObject, config)

enum class VerifyType {
    SIGNUP,
    RECOVERY,
    INVITE
}

/**
 * The Auth plugin handles everything related to supabase's authentication system
 */
val SupabaseClient.auth: Auth
    get() = pluginManager.getPlugin(Auth.key)
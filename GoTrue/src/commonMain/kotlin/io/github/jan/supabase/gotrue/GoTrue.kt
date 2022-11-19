package io.github.jan.supabase.gotrue

import io.github.aakira.napier.Napier
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.admin.AdminApi
import io.github.jan.supabase.gotrue.providers.AuthProvider
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.DefaultAuthProvider
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.Phone
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.plugins.MainConfig
import io.github.jan.supabase.plugins.MainPlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Plugin to interact with the supabase Auth API
 */
sealed interface GoTrue : MainPlugin<GoTrue.Config> {

    /**
     * Returns the current session status
     */
    val sessionStatus: StateFlow<SessionStatus>

    /**
     * Whether the [sessionStatus] session is getting refreshed automatically
     */
    val isAutoRefreshRunning: Boolean

    /**
     * Returns the session manager instance
     */
    val sessionManager: SessionManager

    /**
     * Access to the auth admin api where you can manage users. Service role access token is required. Import it via [importAuthToken]. Never share it publicly
     */
    val admin: AdminApi

    /**
     * Signs up a new user with the specified [provider]
     * @param provider the provider to use for signing up. E.g. [Email], [Phone] or [Google]
     * @param redirectUrl The redirect url to use. If you don't specify this, the platform specific will be use, like deeplinks on android.
     * @param config The configuration to use for the sign-up.
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun <C, R, Provider : AuthProvider<C, R>> signUpWith(
        provider: Provider,
        redirectUrl: String? = null,
        config: (C.() -> Unit)? = null
    ): R

    /**
     * Logins the user with the specified [provider]
     * @param provider the provider to use for signing up. E.g. [Email], [Phone] or [Google]
     * @param redirectUrl The redirect url to use. If you don't specify this, the platform specific will be use, like deeplinks on android.
     * @param config The configuration to use for the sign-up.
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun <C, R, Provider : AuthProvider<C, R>> loginWith(
        provider: Provider,
        redirectUrl: String? = null,
        config: (C.() -> Unit)? = null
    )

    /**
     * Modifies a user with the specified [provider]. Extra data can be supplied
     * @param provider The provider to use. Either [Email] or [Phone]
     * @param extraData Extra data to store
     * @param config The configuration to use
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun <C, R, Provider : DefaultAuthProvider<C, R>> modifyUser(
        provider: Provider,
        extraData: JsonObject? = null,
        config: C.() -> Unit = {}
    ): UserInfo

    /**
     * Sends a one time password to the specified [provider]
     * @param provider The provider to use. Either [Email] or [Phone]
     * @param createUser Whether to create a user when a user with the given credentials doesn't exist
     * @param redirectUrl The redirect url to use. If you don't specify this, the platform specific will be use, like deeplinks on android.
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun <C, R, Provider : DefaultAuthProvider<C, R>> sendOtpTo(
        provider: Provider,
        createUser: Boolean = false,
        redirectUrl: String? = null,
        config: C.() -> Unit
    )

    /**
     * Sends a password reset email to the user with the specified [email]
     * @param email The email to send the password reset email to
     * @param redirectUrl The redirect url to use. If you don't specify this, the platform specific will be use, like deeplinks on android.
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun sendRecoveryEmail(email: String, redirectUrl: String? = null, captchaToken: String? = null)

    /**
     * Sends a nonce to the user's email (preferred) or phone
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun reauthenticate()

    /**
     * Revokes all refresh tokens for the user, and invalidates the session
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun invalidateAllRefreshTokens()

    /**
     * Verifies a registration, invite or password recovery
     * @param type The type of the verification
     * @param token The token used to verify
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun verify(type: VerifyType, token: String, captchaToken: String? = null)

    /**
     * Verifies a phone/sms otp
     * @param token The otp to verify
     * @param phoneNumber The phone number the token was sent to
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun verifyPhone(token: String, phoneNumber: String, captchaToken: String? = null)

    /**
     * Retrieves the current user with the session
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun getUser(jwt: String): UserInfo

    /**
     * Invalidates the current session, which means [sessionStatus] will be [SessionStatus.NotAuthenticated]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
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
    suspend fun importAuthToken(jwt: String) = importSession(UserSession(jwt, "", 0L, "", tryToGetUser(jwt)), false)

    /**
     * Retrieves the latest session from storage and starts auto-refreshing if [autoRefresh] is true or [GoTrue.Config.alwaysAutoRefresh] as the default parameter
     * @return true, if a session was found, false otherwise
     */
    suspend fun loadFromStorage(autoRefresh: Boolean = config.alwaysAutoRefresh): Boolean

    /**
     * Refreshes a session using the refresh token
     * @param refreshToken The refresh token to use
     * @return A new session
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun refreshSession(refreshToken: String): UserSession

    /**
     * Refreshes the current session
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun refreshCurrentSession()

    /**
     * Starts auto-refreshing [session] for [currentSession]
     * @param session The session to auto-refresh
     */
    suspend fun startAutoRefresh(session: UserSession, autoRefresh: Boolean = config.alwaysAutoRefresh)

    /**
     * Starts auto refreshing the current session
     */
    suspend fun startAutoRefreshForCurrentSession()

    /**
     * Stops auto-refreshing the current session
     */
    fun stopAutoRefreshForCurrentSession()

    /**
     * Returns the current access token, or null if no session is available
     */
    fun currentAccessTokenOrNull() = currentSessionOrNull()?.accessToken

    /**
     * Returns the current session or null
     */
    fun currentSessionOrNull() = when(val status = sessionStatus.value) {
        is SessionStatus.Authenticated -> status.session
        else -> null
    }

    data class Config(
        val params: MutableMap<String, Any> = mutableMapOf(),
        var retryDelay: Duration = 10.seconds,
        var alwaysAutoRefresh: Boolean = true,
        var autoLoadFromStorage: Boolean = true,
        override var customUrl: String? = null,
        override var jwtToken: String? = null,
        var sessionManager: SessionManager? = null,
        var coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
    ) : MainConfig

    companion object : SupabasePluginProvider<Config, GoTrue> {

        override val key = "auth"
        const val API_VERSION = 1

        override fun createConfig(init: Config.() -> Unit) = Config().apply(init)
        override fun create(supabaseClient: SupabaseClient, config: Config): GoTrue = GoTrueImpl(supabaseClient, config)

    }

}

/**
 * Modifies a user with the specified [provider]. Extra data can be supplied
 * @param provider The provider to use
 * @param config The configuration to use
 * @param extraData The extra data to use
 * @throws RestException If the current session is invalid
 */
suspend inline fun <C, R, reified D, Provider : DefaultAuthProvider<C, R>> GoTrue.modifyUser(
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
val SupabaseClient.gotrue: GoTrue
    get() = pluginManager.getPlugin(GoTrue)

private suspend fun GoTrue.tryToGetUser(jwt: String) = try {
    getUser(jwt)
} catch (e: Exception) {
    Napier.e(e) { "Couldn't retrieve user using your custom jwt token. If you use the project secret ignore this message" }
    null
}
package io.github.jan.supabase.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.admin.AdminApi
import io.github.jan.supabase.auth.event.AuthEvent
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.auth.mfa.MfaApi
import io.github.jan.supabase.auth.providers.AuthProvider
import io.github.jan.supabase.auth.providers.ExternalAuthConfigDefaults
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.OAuthProvider
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.Phone
import io.github.jan.supabase.auth.providers.builtin.SSO
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.auth.user.UserUpdateBuilder
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.logging.SupabaseLogger
import io.github.jan.supabase.logging.e
import io.github.jan.supabase.plugins.CustomSerializationPlugin
import io.github.jan.supabase.plugins.MainPlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonObject

/**
 * Plugin to interact with the Supabase Auth API
 *
 * To use it you need to install it to the [SupabaseClient]:
 * ```kotlin
 * val supabase = createSupabaseClient(supabaseUrl, supabaseKey) {
 *    install(Auth)
 * }
 * ```
 *
 * then you can use it like this:
 * ```kotlin
 * val result = supabase.auth.signUpWith(Email) {
 *   email = "example@email.com"
 *   password = "password"
 * }
 * ```
 */
interface Auth : MainPlugin<AuthConfig>, CustomSerializationPlugin {

    /**
     * Returns the current session status
     */
    val sessionStatus: StateFlow<SessionStatus>

    /**
     * Events emitted by the auth plugin
     */
    @SupabaseExperimental
    val events: SharedFlow<AuthEvent>

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
     * Access to the mfa api where you can manage multi-factor authentication for the current user.
     */
    val mfa: MfaApi

    /**
     * The cache for the code verifier. This is used for PKCE authentication. Can be customized via [AuthConfig.codeVerifierCache]
     */
    val codeVerifierCache: CodeVerifierCache

    /**
     * Signs up a new user with the specified [provider]
     *
     * Example:
     * ```kotlin
     * val result = auth.signUpWith(Email) {
     *    email = "example@email.com"
     *    password = "password"
     * }
     * ```
     * or
     * ```kotlin
     * auth.signUpWith(Google) // Opens the browser to login with google
     * ```
     *
     * @param provider the provider to use for signing up. E.g. [Email], [Phone] or [Google]
     * @param redirectUrl The redirect url to use. If you don't specify this, the platform specific will be used, like deeplinks on android.
     * @param config The configuration to use for the sign-up.
     * @return The result of the sign-up (e.g. the user id) or null if auto-confirm is enabled (resulting in a login)
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     * @throws AuthWeakPasswordException if using the [Email] or [Phone] provider and the password is too weak. You can get the reasons via [AuthWeakPasswordException.reasons]
     */
    suspend fun <C, R, Provider : AuthProvider<C, R>> signUpWith(
        provider: Provider,
        redirectUrl: String? = defaultRedirectUrl(),
        config: (C.() -> Unit)? = null
    ): R?

    /**
     * Signs in the user with the specified [provider]
     *
     * Example:
     * ```kotlin
     * val result = auth.signInWith(Email) {
     *    email = "example@email.com"
     *    password = "password"
     * }
     * ```
     * or
     * ```kotlin
     * auth.signInWith(Google) // Opens the browser to login with google
     * ```
     *
     * @param provider the provider to use for signing in. E.g. [Email], [Phone] or [Google]
     * @param redirectUrl The redirect url to use. If you don't specify this, the platform specific will be used, like deeplinks on android.
     * @param config The configuration to use for the sign-in.
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun <C, R, Provider : AuthProvider<C, R>> signInWith(
        provider: Provider,
        redirectUrl: String? = defaultRedirectUrl(),
        config: (C.() -> Unit)? = null
    )

    /**
     * Signs in the user without any credentials. This will create a new user session with a new access token.
     *
     * If you want to upgrade this anonymous user to a real user, use [linkIdentity] to link an OAuth identity or [updateUser] to add an email or phone.
     *
     * @param data Extra data for the user
     * @param captchaToken The captcha token to use
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun signInAnonymously(data: JsonObject? = null, captchaToken: String? = null)

    /**
     * Links an OAuth Identity to an existing user.
     *
     * This methods works similar to signing in with OAuth providers. Refer to the [documentation](https://supabase.com/docs/reference/kotlin/initializing) to learn how to handle OAuth and OTP links.
     * @param provider The OAuth provider
     * @param redirectUrl The redirect url to use. If you don't specify this, the platform specific will be used, like deeplinks on android.
     * @param config Extra configuration
     * @return The OAuth url to open in the browser if [ExternalAuthConfigDefaults.automaticallyOpenUrl] is false, otherwise null.
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun linkIdentity(
        provider: OAuthProvider,
        redirectUrl: String? = defaultRedirectUrl(),
        config: ExternalAuthConfigDefaults.() -> Unit = {}
    ): String?

    /**
     * Unlinks an OAuth Identity from an existing user.
     * @param identityId The id of the OAuth identity
     * @param updateLocalUser Whether to delete the identity from the local user or not
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun unlinkIdentity(
        identityId: String,
        updateLocalUser: Boolean = true
    )

    /**
     * Retrieves the sso url for the given [config]
     * @param redirectUrl The redirect url to use
     * @param config The configuration to use
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun retrieveSSOUrl(redirectUrl: String? = defaultRedirectUrl(), config: SSO.Config.() -> Unit): SSO.Result

    /**
     * Modifies the current user
     * @param updateCurrentUser Whether to update the current user in the [SupabaseClient]
     * @param config The configuration to use
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun updateUser(
        updateCurrentUser: Boolean = true,
        redirectUrl: String? = defaultRedirectUrl(),
        config: UserUpdateBuilder.() -> Unit
    ): UserInfo

    /**
     * Resends an existing signup confirmation email, email change email
     * @param type The email otp type
     * @param email The email to resend the otp to
     * @param captchaToken The captcha token to use
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun resendEmail(type: OtpType.Email, email: String, captchaToken: String? = null)

    /**
     * Resends an existing SMS OTP or phone change OTP.
     * @param type The phone otp type
     * @param phone The phone to resend the otp to
     * @param captchaToken The captcha token to use
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun resendPhone(type: OtpType.Phone, phone: String, captchaToken: String? = null)

    /**
     * Sends a password reset email to the user with the specified [email]
     * @param email The email to send the password reset email to
     * @param redirectUrl The redirect url to use. If you don't specify this, the platform specific will be used, like deeplinks on android.
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun resetPasswordForEmail(email: String, redirectUrl: String? = defaultRedirectUrl(), captchaToken: String? = null)

    /**
     * Sends a nonce to the user's email (preferred) or phone
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun reauthenticate()

    /**
     * Verifies a email otp
     * @param type The type of the verification
     * @param email The email to verify
     * @param token The token used to verify
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun verifyEmailOtp(type: OtpType.Email, email: String, token: String, captchaToken: String? = null)

    /**
     * Verifies a email otp token hash received via email
     * @param type The type of the verification
     * @param tokenHash The token hash used to verify
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun verifyEmailOtp(type: OtpType.Email, tokenHash: String, captchaToken: String? = null)

    /**
     * Verifies a phone/sms otp
     * @param type The type of the verification
     * @param token The otp to verify
     * @param phone The phone number the token was sent to
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun verifyPhoneOtp(type: OtpType.Phone, phone: String, token: String, captchaToken: String? = null)

    /**
     * Retrieves the user attached to the specified [jwt]
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun retrieveUser(jwt: String): UserInfo

    /**
     * Retrieves the current user with the current session
     * @param updateSession Whether to update [sessionStatus] with the updated user, if [sessionStatus] is [SessionStatus.Authenticated]
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun retrieveUserForCurrentSession(updateSession: Boolean = false): UserInfo

    /**
     * Signs out the current user, which means [sessionStatus] will be [SessionStatus.NotAuthenticated] and the access token will be revoked
     * @param scope The scope of the sign-out.
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     * @see SignOutScope
     */
    suspend fun signOut(scope: SignOutScope = SignOutScope.LOCAL)

    /**
     * Imports a user session and starts auto-refreshing if [autoRefresh] is true
     */
    suspend fun importSession(session: UserSession, autoRefresh: Boolean = config.alwaysAutoRefresh, source: SessionSource = SessionSource.Unknown)

    /**
     * Imports the jwt token and retrieves the user profile.
     * Be aware auto-refreshing is not available when importing **only** a jwt token.
     * @param accessToken The jwt token to import
     * @param retrieveUser Whether to retrieve the user profile or not
     */
    suspend fun importAuthToken(accessToken: String, refreshToken: String = "", retrieveUser: Boolean = false, autoRefresh: Boolean = if(refreshToken.isNotBlank()) config.alwaysAutoRefresh else false) = importSession(UserSession(accessToken, refreshToken, "", "", 0L, "", if(retrieveUser) tryToGetUser(accessToken) else null), autoRefresh)

    /**
     * Retrieves the latest session from storage and starts auto-refreshing if [autoRefresh] is true or [AuthConfig.alwaysAutoRefresh] as the default parameter
     * @return true, if a session was found, false otherwise
     */
    suspend fun loadFromStorage(autoRefresh: Boolean = config.alwaysAutoRefresh): Boolean

    /**
     * Refreshes a session using the refresh token
     * @param refreshToken The refresh token to use
     * @return A new session
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun refreshSession(refreshToken: String): UserSession

    /**
     * Refreshes the current session
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun refreshCurrentSession()

    /**
     * Deletes the current session from storage and sets [sessionStatus] to [SessionStatus.NotAuthenticated]
     */
    suspend fun clearSession()

    /**
     * Sets the session status to the specified [status]
     */
    @SupabaseInternal
    fun setSessionStatus(status: SessionStatus)

    /**
     * Emits an event to the [events] flow
     */
    @SupabaseInternal
    fun emitEvent(event: AuthEvent)

    /**
     * Exchanges a code for a session. Used when using the [FlowType.PKCE] flow
     * @param code The code to exchange
     * @param saveSession Whether to save the session in storage
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun exchangeCodeForSession(code: String, saveSession: Boolean = true): UserSession

    /**
     * Starts auto refreshing the current session
     */
    suspend fun startAutoRefreshForCurrentSession()

    /**
     * Returns the url to use for oAuth
     * @param provider The provider to use
     * @param redirectUrl The redirect url to use
     * @param url The url suffix.
     *
     * For normal OAuth it would be "authorize".
     *
     * For linking identities it would be "user/identities/authorize"
     */
    fun getOAuthUrl(provider: OAuthProvider, redirectUrl: String? = defaultRedirectUrl(), url: String = "authorize", additionalConfig: ExternalAuthConfigDefaults.() -> Unit = {}): String

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

    /**
     * Returns the current user or null
     */
    fun currentUserOrNull() = currentSessionOrNull()?.user

    /**
     * Returns the connected identities to the current user or null
     */
    fun currentIdentitiesOrNull() = currentUserOrNull()?.identities

    /**
     * Blocks the current coroutine until the plugin is initialized.
     *
     * This will make sure that the [SessionStatus] is set to [SessionStatus.Authenticated], [SessionStatus.NotAuthenticated] or [SessionStatus.RefreshError].
     */
    suspend fun awaitInitialization()

    companion object : SupabasePluginProvider<AuthConfig, Auth> {

        internal val HASH_PARAMETERS = listOf(
            "access_token",
            "refresh_token",
            "expires_in",
            "expires_at",
            "token_type",
            "type",
            "provider_refresh_token",
            "provider_token",
            "error",
            "error_code",
            "error_description",
        )

        internal val QUERY_PARAMETERS = listOf(
            "code",
            "error_code",
            "error",
            "error_description",
        )

        override val key = "auth"

        override val logger: SupabaseLogger = SupabaseClient.createLogger("Supabase-Auth")

        /**
         * The auth api version to use
         */
        const val API_VERSION = 1

        override fun createConfig(init: AuthConfig.() -> Unit) = AuthConfig().apply(init)
        override fun create(supabaseClient: SupabaseClient, config: AuthConfig): Auth = AuthImpl(supabaseClient, config)

    }

}

/**
 * The Auth plugin handles everything related to Supabase's authentication system
 */
val SupabaseClient.auth: Auth
    get() = pluginManager.getPlugin(Auth)

private suspend fun Auth.tryToGetUser(jwt: String) = try {
    retrieveUser(jwt)
} catch (e: Exception) {
    currentCoroutineContext().ensureActive()
    Auth.logger.e(e) { "Couldn't retrieve user using your custom jwt token. If you use the project secret ignore this message" }
    null
}
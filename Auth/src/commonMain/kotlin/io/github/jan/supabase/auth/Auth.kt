package io.github.jan.supabase.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.admin.AdminApi
import io.github.jan.supabase.auth.api.ResolveAccessToken
import io.github.jan.supabase.auth.event.AuthEvent
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.InvalidJwtException
import io.github.jan.supabase.auth.exception.TokenExpiredException
import io.github.jan.supabase.auth.jwt.ClaimsRequestBuilder
import io.github.jan.supabase.auth.jwt.ClaimsResponse
import io.github.jan.supabase.auth.mfa.MfaApi
import io.github.jan.supabase.auth.providers.Email
import io.github.jan.supabase.auth.providers.EmailSignInOtpConfig
import io.github.jan.supabase.auth.providers.EmailSignUpConfig
import io.github.jan.supabase.auth.providers.LoginIdentifier
import io.github.jan.supabase.auth.providers.Phone
import io.github.jan.supabase.auth.providers.PhoneSignInOtpConfig
import io.github.jan.supabase.auth.providers.PhoneSignUpConfig
import io.github.jan.supabase.auth.providers.SignInPasswordConfig
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.auth.user.UserUpdateBuilder
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.network.SupabaseApi
import io.github.jan.supabase.plugins.CustomSerializationPlugin
import io.github.jan.supabase.plugins.MainPlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CoroutineScope
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
     * The coroutine scope used for background auth requests (e.g. session refreshes)
     */
    @SupabaseInternal
    val authScope: CoroutineScope

    val userApi: SupabaseApi

    suspend fun signUp(
        identifier: Email,
        password: String,
        config: EmailSignUpConfig.() -> Unit = {}
    ): AuthResponse

    suspend fun signUp(
        identifier: Phone,
        password: String,
        config: PhoneSignUpConfig.() -> Unit = {}
    ): AuthResponse

    suspend fun signInWithPassword(
        identifier: LoginIdentifier,
        password: String,
        config: SignInPasswordConfig.() -> Unit = {}
    ): UserSession

    suspend fun signInWithIdToken(
        provider: IDTokenProvider,
        token: String,
        config: IdTokenConfig.() -> Unit = {}
    ): UserSession {
        val config = DefaultIdTokenConfig(provider, token).apply(config)
        return signInWithIdToken(config)
    }

    suspend fun signInWithIdToken(
        config: IdTokenConfig
    ): UserSession

    suspend fun signInWithOtp(
        identifier: Email,
        config: EmailSignInOtpConfig.() -> Unit
    )

    suspend fun signInWithOtp(
        identifier: Phone,
        config: PhoneSignInOtpConfig.() -> Unit
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
    suspend fun signInAnonymously(data: JsonObject? = null, captchaToken: String? = null): UserSession

    /**
     * Links an identity to the current user using an ID token.
     *
     * Example:
     * ```kotlin
     * supabase.auth.linkIdentityWithIdToken(provider = Google, idToken = "idToken") {
     *     // Optional nonce
     *     nonce = "nonce"
     * }
     * ```
     *
     * @param provider One of the [IDTokenProvider] providers.
     * @param idToken The ID token to use
     * @param config Extra configuration
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun linkIdentityWithIdToken(
        provider: IDTokenProvider,
        idToken: String,
        config: (IdTokenConfig).() -> Unit = {}
    ): UserSession

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
    suspend fun retrieveSSOUrl(identifier: SSOIdentifier, config: SSOConfig.() -> Unit): String

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
     * @param redirectUrl The redirect Url
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun resendEmail(type: OtpType.Email, email: String, captchaToken: String? = null, redirectUrl: String? = defaultRedirectUrl())

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
     * @return [OtpVerifyResult.Authenticated] if the OTP was verified and a session was returned.
     *
     * [OtpVerifyResult.VerifiedNoSession] if the session was verified but no session was returned (for example when changing the E-Mail with the "Secure email change enabled" option enabled)
     * @see OtpVerifyResult.VerifiedNoSession
     * @see OtpVerifyResult.Authenticated
     */
    suspend fun verifyEmailOtp(type: OtpType.Email, email: String, token: String, captchaToken: String? = null): OtpVerifyResult

    /**
     * Verifies an email otp token hash received via email
     * @param type The type of the verification
     * @param tokenHash The token hash used to verify
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     * @return [OtpVerifyResult.Authenticated] if the OTP was verified and a session was returned.
     *
     * [OtpVerifyResult.VerifiedNoSession] if the session was verified but no session was returned (for example when changing the E-Mail with the "Secure email change enabled" option enabled)
     * @see OtpVerifyResult.VerifiedNoSession
     * @see OtpVerifyResult.Authenticated
     */
    suspend fun verifyEmailOtp(type: OtpType.Email, tokenHash: String, captchaToken: String? = null): OtpVerifyResult

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
    suspend fun getUser(jwt: String): UserInfo

    /**
     * Retrieves the current user with the current sessioretrieveUsn
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
     * @throws RestException or one of its subclasses if receiving an error response. If the error response contains a error code, an [AuthRestException] will be thrown which can be used to easier identify the problem.
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun exchangeCodeForSession(code: String): UserSession

    /**
     * Starts auto refreshing the current session
     */
    suspend fun startAutoRefreshForCurrentSession()

    /**
     * Extracts the JWT claims present in the access token by first verifying the
     * JWT against the server's JSON Web Key Set endpoint
     * `/.well-known/jwks.json` which is often cached, resulting in significantly
     * faster responses. Prefer this method over [getUser] which always
     * sends a request to the Auth server for each JWT.
     *
     * If the project is not using an asymmetric JWT signing key (like ECC or
     * RSA) it always sends a request to the Auth server (similar to [getUser]) to verify the JWT.
     *
     * @param jwt An optional specific JWT you wish to verify, not the one you
     *            can obtain from [currentSessionOrNull].
     * @param options Various additional options that allow you to customize the
     *                behavior of this method.
     * @throws TokenExpiredException when trying to get the claims of an expired [jwt] and [ClaimsRequestBuilder.allowExpired] is set to false
     * @throws InvalidJwtException if the [jwt] is invalid
     * @throws AuthRestException on any REST-related error responses during the fetching of the JWKs or retrieving of the current user data
     */
    suspend fun getClaims(
        jwt: String? = null,
        options: ClaimsRequestBuilder.() -> Unit = {}
    ): ClaimsResponse

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
    fun getOAuthUrl(provider: OAuthProvider, url: String = "authorize", additionalConfig: OAuthConfig.() -> Unit = {}): String =
        getOAuthUrl(provider, url, DefaultOAuthConfig().apply(additionalConfig))

    fun getOAuthUrl(provider: OAuthProvider, url: String = "authorize", additionalConfig: OAuthConfig): String

    /**
     * Stops auto-refreshing the current session
     */
    fun stopAutoRefreshForCurrentSession()

    /**
     * Returns debug information about the state of the auto-refresher.
     * If the value is null, the auto-refresh never started.
     */
    @SupabaseInternal
    fun autoRefreshInformation(): SessionRefreshInformation?

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

    @SupabaseInternal
    fun defaultRedirectUrl(): String?

    /**
     * Blocks the current coroutine until the plugin is initialized.
     *
     * This will make sure that the [SessionStatus] is set to [SessionStatus.Authenticated], [SessionStatus.NotAuthenticated] or [SessionStatus.RefreshError].
     */
    suspend fun awaitInitialization()

    companion object : SupabasePluginProvider<AuthConfig, Auth> {

        override val key = "auth"

        /**
         * The tag for the Auth logger.
         */
        const val LOGGING_TAG = "Supabase-Auth"

        /**
         * The auth api version to use
         */
        const val API_VERSION = 1

        @SupabaseInternal
        fun defaultResolveAccessToken(supabaseClient: SupabaseClient): ResolveAccessToken = { token, fallback -> supabaseClient.resolveAccessToken(token, fallback) }

        override fun createConfig(init: AuthConfig.() -> Unit) = AuthConfig().apply(init)

        override fun create(supabaseClient: SupabaseClient, config: AuthConfig): Auth = AuthImpl(supabaseClient, config)

    }

}

/**
 * The Auth plugin handles everything related to Supabase's authentication system
 */
val SupabaseClient.auth: Auth
    get() = pluginManager.getPlugin(Auth)
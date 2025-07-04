package io.github.jan.supabase.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.admin.AdminApi
import io.github.jan.supabase.auth.admin.AdminApiImpl
import io.github.jan.supabase.auth.event.AuthEvent
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthSessionMissingException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.auth.mfa.MfaApi
import io.github.jan.supabase.auth.mfa.MfaApiImpl
import io.github.jan.supabase.auth.providers.AuthProvider
import io.github.jan.supabase.auth.providers.ExternalAuthConfigDefaults
import io.github.jan.supabase.auth.providers.OAuthProvider
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.providers.builtin.SSO
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.auth.user.UserUpdateBuilder
import io.github.jan.supabase.bodyOrNull
import io.github.jan.supabase.exceptions.BadRequestRestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.UnauthorizedRestException
import io.github.jan.supabase.exceptions.UnknownRestException
import io.github.jan.supabase.logging.d
import io.github.jan.supabase.logging.e
import io.github.jan.supabase.logging.i
import io.github.jan.supabase.putJsonObject
import io.github.jan.supabase.safeBody
import io.github.jan.supabase.supabaseJson
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.coroutines.coroutineContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

private const val SESSION_REFRESH_THRESHOLD = 0.8
@Suppress("MagicNumber") // see #631
private val SIGNOUT_IGNORE_CODES = listOf(401, 403, 404)

@PublishedApi
internal class AuthImpl(
    override val supabaseClient: SupabaseClient,
    override val config: AuthConfig
) : Auth {

    private val _sessionStatus = MutableStateFlow<SessionStatus>(SessionStatus.Initializing)
    override val sessionStatus: StateFlow<SessionStatus> = _sessionStatus.asStateFlow()
    private val _events = MutableSharedFlow<AuthEvent>(replay = 1)
    override val events: SharedFlow<AuthEvent> = _events.asSharedFlow()
    @Suppress("DEPRECATION")
    internal val authScope = CoroutineScope((config.coroutineDispatcher ?: supabaseClient.coroutineDispatcher) + SupervisorJob())
    override val sessionManager = config.sessionManager ?: createDefaultSessionManager()
    override val codeVerifierCache = config.codeVerifierCache ?: createDefaultCodeVerifierCache()

    @OptIn(SupabaseInternal::class)
    internal val api = supabaseClient.authenticatedSupabaseApi(this)
    override val admin: AdminApi = AdminApiImpl(this)
    override val mfa: MfaApi = MfaApiImpl(this)
    var sessionJob: Job? = null
    override val isAutoRefreshRunning: Boolean
        get() = sessionJob?.isActive == true

    override val serializer = config.serializer ?: supabaseClient.defaultSerializer

    override val apiVersion: Int
        get() = Auth.API_VERSION

    override val pluginKey: String
        get() = Auth.key

    init {
        if(supabaseClient.accessToken != null) error("The Auth plugin is not available when using a custom access token provider. Please uninstall the Auth plugin.")
    }

    override fun init() {
        Auth.logger.d { "Initializing Auth plugin..." }
        if (config.autoLoadFromStorage) {
            authScope.launch {
                Auth.logger.i {
                    "Loading session from storage..."
                }
                val successful = loadFromStorage()
                if (successful) {
                    Auth.logger.i {
                        "Successfully loaded session from storage!"
                    }
                } else {
                    Auth.logger.i {
                        "No session found in storage."
                    }
                    setSessionStatus(SessionStatus.NotAuthenticated())
                }
            }
        } else {
            Auth.logger.d { "Skipping loading from storage (autoLoadFromStorage is set to false)" }
            setSessionStatus(SessionStatus.NotAuthenticated())
        }
        setupPlatform()
        Auth.logger.d { "Initialized Auth plugin" }
    }

    override suspend fun <C, R, Provider : AuthProvider<C, R>> signInWith(
        provider: Provider,
        redirectUrl: String?,
        config: (C.() -> Unit)?
    ) = provider.login(supabaseClient, {
        importSession(it, source = SessionSource.SignIn(provider))
    }, redirectUrl, config)

    override suspend fun signInAnonymously(data: JsonObject?, captchaToken: String?) {
        val response = api.postJson("signup", buildJsonObject {
            data?.let { put("data", it) }
            captchaToken?.let(::putCaptchaToken)
        })
        val session = response.safeBody<UserSession>()
        importSession(session, source = SessionSource.AnonymousSignIn)
    }

    override suspend fun <C, R, Provider : AuthProvider<C, R>> signUpWith(
        provider: Provider,
        redirectUrl: String?,
        config: (C.() -> Unit)?
    ): R? = provider.signUp(supabaseClient, {
        importSession(it, source = SessionSource.SignUp(provider))
    }, redirectUrl, config)

    override suspend fun linkIdentity(
        provider: OAuthProvider,
        redirectUrl: String?,
        config: ExternalAuthConfigDefaults.() -> Unit
    ): String? {
        val automaticallyOpen = ExternalAuthConfigDefaults().apply(config).automaticallyOpenUrl
        val fetchUrl: suspend (String?) -> String = { redirectTo: String? ->
            val url = getOAuthUrl(provider, redirectTo, "user/identities/authorize", config)
            val response = api.rawRequest(url) {
                method = HttpMethod.Get
                parameter("skip_http_redirect", true)
            }
            response.body<JsonObject>()["url"]?.jsonPrimitive?.contentOrNull ?: error("No URL found in response")
        }
        if(!automaticallyOpen) {
            return fetchUrl(redirectUrl ?: "")
        }
        startExternalAuth(
            redirectUrl = redirectUrl,
            getUrl = {
                fetchUrl(it)
            },
            onSessionSuccess = {
                importSession(it, source = SessionSource.UserIdentitiesChanged(it))
            }
        )
        return null
    }

    override suspend fun unlinkIdentity(identityId: String, updateLocalUser: Boolean) {
        api.delete("user/identities/$identityId")
        if (updateLocalUser) {
            val session = currentSessionOrNull() ?: return
            val newUser = session.user?.copy(identities = session.user.identities?.filter { it.identityId != identityId })
            val newSession = session.copy(user = newUser)
            setSessionStatus(SessionStatus.Authenticated(newSession, SessionSource.UserIdentitiesChanged(session)))
        }
    }

    override suspend fun retrieveSSOUrl(
        redirectUrl: String?,
        config: SSO.Config.() -> Unit
    ): SSO.Result {
        val createdConfig = SSO.Config().apply(config)

        require((createdConfig.domain != null && createdConfig.domain!!.isNotBlank()) || (createdConfig.providerId != null && createdConfig.providerId!!.isNotBlank())) {
            "Either domain or providerId must be set"
        }

        require(createdConfig.domain == null || createdConfig.providerId == null) {
            "Either domain or providerId must be set, not both"
        }

        val codeChallenge: String? = preparePKCEIfEnabled()
        return api.postJson("sso", buildJsonObject {
            redirectUrl?.let { put("redirect_to", it) }
            createdConfig.captchaToken?.let(::putCaptchaToken)
            codeChallenge?.let(::putCodeChallenge)
            createdConfig.domain?.let {
                put("domain", it)
            }
            createdConfig.providerId?.let {
                put("provider_id", it)
            }
        }).body()
    }

    override suspend fun updateUser(
        updateCurrentUser: Boolean,
        redirectUrl: String?,
        config: UserUpdateBuilder.() -> Unit
    ): UserInfo {
        val updateBuilder = UserUpdateBuilder(serializer = serializer).apply(config)
        val codeChallenge = preparePKCEIfEnabled()
        val body = buildJsonObject {
            putJsonObject(supabaseJson.encodeToJsonElement(updateBuilder).jsonObject)
            codeChallenge?.let(::putCodeChallenge)
        }.toString()
        val response = api.putJson("user", body) {
            redirectUrl?.let { url.parameters.append("redirect_to", it) }
        }
        val userInfo = response.safeBody<UserInfo>()
        if (updateCurrentUser && sessionStatus.value is SessionStatus.Authenticated) {
            val newSession =
                (sessionStatus.value as SessionStatus.Authenticated).session.copy(user = userInfo)
            if (this.config.autoSaveToStorage) {
                sessionManager.saveSession(newSession)
            }
            setSessionStatus(SessionStatus.Authenticated(newSession, SessionSource.UserChanged(newSession)))
        }
        return userInfo
    }

    private suspend fun resend(type: String, body: JsonObjectBuilder.() -> Unit) {
        api.postJson("resend", buildJsonObject {
            put("type", type)
            putJsonObject(buildJsonObject(body))
        })
    }

    override suspend fun resendEmail(type: OtpType.Email, email: String, captchaToken: String?) =
        resend(type.type) {
            put("email", email)
            captchaToken?.let(::putCaptchaToken)
        }

    override suspend fun resendPhone(
        type: OtpType.Phone,
        phone: String,
        captchaToken: String?
    ) = resend(type.type) {
        put("phone", phone)
        captchaToken?.let(::putCaptchaToken)
    }

    override suspend fun resetPasswordForEmail(
        email: String,
        redirectUrl: String?,
        captchaToken: String?
    ) {
        require(email.isNotBlank()) {
            "Email must not be blank"
        }
        val codeChallenge = preparePKCEIfEnabled()
        val body = buildJsonObject {
            put("email", email)
            captchaToken?.let(::putCaptchaToken)
            codeChallenge?.let(::putCodeChallenge)
        }.toString()
        api.postJson("recover", body) {
            redirectUrl?.let { url.encodedParameters.append("redirect_to", it) }
        }
    }

    override suspend fun reauthenticate() {
        api.get("reauthenticate")
    }

    override suspend fun signOut(scope: SignOutScope) {
        if (currentSessionOrNull() != null) {
            try {
                api.post("logout") {
                    parameter("scope", scope.name.lowercase())
                }
            } catch(e: RestException) {
                if(e.statusCode in SIGNOUT_IGNORE_CODES) {
                    Auth.logger.d { "Received error code ${e.statusCode} while signing out user. This can happen if the user doesn't exist anymore or the JWT is invalid/expired. Proceeding to clean up local data..." }
                } else throw e
            }
            Auth.logger.d { "Logged out session in Supabase" }
        } else {
            Auth.logger.i { "Skipping session logout as there is no session available. Proceeding to clean up local data..." }
        }
        if (scope != SignOutScope.OTHERS) {
            clearSession()
        }
        Auth.logger.d { "Successfully logged out" }
    }

    private suspend fun verify(
        type: String,
        token: String?,
        captchaToken: String?,
        additionalData: JsonObjectBuilder.() -> Unit
    ) {
        val body = buildJsonObject {
            put("type", type)
            token?.let { put("token", it) }
            captchaToken?.let(::putCaptchaToken)
            additionalData()
        }
        val response = api.postJson("verify", body)
        val session = response.body<UserSession>()
        importSession(session, source = SessionSource.SignIn(OTP))
    }

    override suspend fun verifyEmailOtp(
        type: OtpType.Email,
        email: String,
        token: String,
        captchaToken: String?
    ) = verify(type.type, token, captchaToken) {
        put("email", email)
    }

    override suspend fun verifyEmailOtp(
        type: OtpType.Email,
        tokenHash: String,
        captchaToken: String?
    ) = verify(type.type, null, captchaToken) {
        put("token_hash", tokenHash)
    }

    override suspend fun verifyPhoneOtp(
        type: OtpType.Phone,
        phone: String,
        token: String,
        captchaToken: String?
    ) = verify(type.type, token, captchaToken) {
        put("phone", phone)
    }

    override suspend fun retrieveUser(jwt: String): UserInfo {
        val response = api.get("user") {
            headers["Authorization"] = "Bearer $jwt"
        }
        val body = response.bodyAsText()
        return supabaseJson.decodeFromString(body)
    }

    override suspend fun retrieveUserForCurrentSession(updateSession: Boolean): UserInfo {
        val user = retrieveUser(currentAccessTokenOrNull() ?: error("No session found"))
        if (updateSession) {
            val session = currentSessionOrNull() ?: error("No session found")
            val newStatus = SessionStatus.Authenticated(session.copy(user = user), SessionSource.UserChanged(currentSessionOrNull() ?: error("Session shouldn't be null")))
            setSessionStatus(newStatus)
            if (config.autoSaveToStorage) sessionManager.saveSession(newStatus.session)
        }
        return user
    }

    override suspend fun exchangeCodeForSession(code: String, saveSession: Boolean): UserSession {
        val codeVerifier = codeVerifierCache.loadCodeVerifier()
        require(codeVerifier != null) {
            "No code verifier stored. Make sure to use `getOAuthUrl` for the OAuth Url to prepare the PKCE flow."
        }
        val session = api.postJson("token?grant_type=pkce", buildJsonObject {
            put("auth_code", code)
            put("code_verifier", codeVerifier)
        }) {
            headers.remove("Authorization")
        }.safeBody<UserSession>()
        codeVerifierCache.deleteCodeVerifier()
        if (saveSession) {
            importSession(session, source = SessionSource.External)
        }
        return session
    }

    override suspend fun refreshSession(refreshToken: String): UserSession {
        Auth.logger.d {
            "Refreshing session"
        }
        val body = buildJsonObject {
            put("refresh_token", refreshToken)
        }
        val response = api.postJson("token?grant_type=refresh_token", body) {
            headers.remove("Authorization")
        }
        return response.safeBody("Auth#refreshSession")
    }

    override suspend fun refreshCurrentSession() {
        val newSession = refreshSession(
            currentSessionOrNull()?.refreshToken
                ?: error("No refresh token found in current session")
        )
        importSession(newSession, source = SessionSource.Refresh(currentSessionOrNull() ?: error("No session found")))
    }

    override suspend fun importSession(
        session: UserSession,
        autoRefresh: Boolean,
        source: SessionSource
    ) {
        Auth.logger.d { "Importing session $session from $source, auto refresh is set to $autoRefresh." }
        if (!autoRefresh) {
            if (session.refreshToken.isNotBlank() && session.expiresIn != 0L && config.autoSaveToStorage) {
                sessionManager.saveSession(session)
                Auth.logger.d { "Session saved to storage (no auto refresh)" }
            }
            setSessionStatus(SessionStatus.Authenticated(session, source))
            Auth.logger.d { "Session imported successfully." }
            return
        }
        val thresholdDate = session.expiresAt - session.expiresIn.seconds * (1 - SESSION_REFRESH_THRESHOLD)
        if (thresholdDate <= Clock.System.now()) {
            Auth.logger.d { "Session is under the threshold date. Refreshing session..." }
            tryImportingSession(
                { handleExpiredSession(session, config.alwaysAutoRefresh) },
                { importSession(session) },
                { updateStatusIfExpired(session, it) }
            )
        } else {
            if (config.autoSaveToStorage) {
                sessionManager.saveSession(session)
                Auth.logger.d { "Session saved to storage (auto refresh enabled)" }
            }
            setSessionStatus(SessionStatus.Authenticated(session, source))
            Auth.logger.d { "Session imported successfully. Starting auto refresh..." }
            sessionJob?.cancel()
            sessionJob = authScope.launch {
                delayBeforeExpiry(session)
                launch {
                    tryImportingSession(
                        { handleExpiredSession(session) },
                        { importSession(session, source = source) },
                        { updateStatusIfExpired(session, it) }
                    )
                }
            }
            Auth.logger.d { "Auto refresh started." }
        }
    }

    @Suppress("MagicNumber")
    private suspend fun tryImportingSession(
        importRefreshedSession: suspend () -> Unit,
        retry: suspend () -> Unit,
        updateStatus: suspend (RefreshFailureCause) -> Unit
    ) {
        try {
            importRefreshedSession()
        } catch (e: RestException) {
            if (e.statusCode in 500..599) {
                Auth.logger.e(e) { "Couldn't refresh session due to an internal server error. Retrying in ${config.retryDelay} (Status code ${e.statusCode})..." }
                updateStatus(RefreshFailureCause.InternalServerError(e))
                delay(config.retryDelay)
                retry()
            } else {
                Auth.logger.e(e) { "Couldn't refresh session. The refresh token may have been revoked. Clearing session (Status code ${e.statusCode})... " }
                clearSession()
            }
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            Auth.logger.e(e) { "Couldn't reach Supabase. Either the address doesn't exist or the network might not be on. Retrying in ${config.retryDelay}..." }
            updateStatus(RefreshFailureCause.NetworkError(e))
            delay(config.retryDelay)
            retry()
        }
    }

    private fun updateStatusIfExpired(session: UserSession, reason: RefreshFailureCause) {
        if (session.expiresAt <= Clock.System.now()) {
            Auth.logger.d { "Session expired while trying to refresh the session. Updating status..." }
            setSessionStatus(SessionStatus.RefreshFailure(reason))
        }
        emitEvent(AuthEvent.RefreshFailure(reason))
    }

    private suspend fun delayBeforeExpiry(session: UserSession) {
        val timeAtBeginningOfSession = session.expiresAt - session.expiresIn.seconds

        // 80% of the way to session.expiresAt
        val targetRefreshTime = timeAtBeginningOfSession + (session.expiresIn.seconds * SESSION_REFRESH_THRESHOLD)

        val delayDuration = targetRefreshTime - Clock.System.now()

        Auth.logger.d {
            "Refreshing session in $delayDuration."
        }
        // if the delayDuration is negative, delay() will not delay
        delay(delayDuration)
    }

    private suspend fun handleExpiredSession(session: UserSession, autoRefresh: Boolean = true) {
        Auth.logger.d {
            "Session expired. Refreshing session..."
        }
        val newSession = refreshSession(session.refreshToken)
        importSession(newSession, autoRefresh, SessionSource.Refresh(session))
    }

    override suspend fun startAutoRefreshForCurrentSession() =
        importSession(currentSessionOrNull() ?: error("No session found"), true, (sessionStatus.value as SessionStatus.Authenticated).source)

    override fun stopAutoRefreshForCurrentSession() {
        Auth.logger.d { "Stopping auto refresh for current session" }
        sessionJob?.cancel()
        sessionJob = null
    }

    override suspend fun loadFromStorage(autoRefresh: Boolean): Boolean {
        val session = sessionManager.loadSession()
        session?.let {
            importSession(it, autoRefresh, SessionSource.Storage)
        }
        return session != null
    }

    override suspend fun close() {
        authScope.cancel()
    }

    override suspend fun parseErrorResponse(response: HttpResponse): RestException {
        val errorBody =
            response.bodyOrNull<GoTrueErrorResponse>() ?: GoTrueErrorResponse("Unknown error", "")
        checkErrorCodes(errorBody, response)?.let { return it }
        return when (response.status) {
            HttpStatusCode.Unauthorized -> UnauthorizedRestException(
                errorBody.error ?: "Unauthorized",
                response,
                errorBody.description
            )
            HttpStatusCode.BadRequest -> BadRequestRestException(
                errorBody.error ?: "Bad Request",
                response,
                errorBody.description
            )
            HttpStatusCode.UnprocessableEntity -> BadRequestRestException(
                errorBody.error ?: "Unprocessable Entity",
                response,
                errorBody.description
            )
            else -> UnknownRestException(errorBody.error ?: "Unknown Error", response)
        }
    }

    private fun checkErrorCodes(error: GoTrueErrorResponse, response: HttpResponse): RestException? {
        return when (error.error) {
            AuthWeakPasswordException.CODE -> AuthWeakPasswordException(error.description, response, error.weakPassword?.reasons ?: emptyList())
            AuthSessionMissingException.CODE -> {
                authScope.launch {
                    Auth.logger.e { "Received session not found api error. Clearing session..." }
                    clearSession()
                }
                AuthSessionMissingException(response)
            }
            else -> {
                error.error?.let { AuthRestException(it, error.description, response) }
            }
        }
    }

    @OptIn(SupabaseExperimental::class)
    override fun getOAuthUrl(
        provider: OAuthProvider,
        redirectUrl: String?,
        url: String,
        additionalConfig: ExternalAuthConfigDefaults.() -> Unit
    ): String {
        val config = ExternalAuthConfigDefaults().apply(additionalConfig)
        val codeChallenge = preparePKCEIfEnabled()
        codeChallenge?.let {
            config.queryParams["code_challenge"] = it
            config.queryParams["code_challenge_method"] = PKCEConstants.CHALLENGE_METHOD
        }
        return resolveUrl(buildString {
            append("$url?provider=${provider.name}&redirect_to=$redirectUrl")
            if (config.scopes.isNotEmpty()) append("&scopes=${config.scopes.joinToString("+")}")
            if (config.queryParams.isNotEmpty()) {
                for ((key, value) in config.queryParams) {
                    append("&$key=$value")
                }
            }
        })
    }

    override suspend fun clearSession() {
        codeVerifierCache.deleteCodeVerifier()
        sessionManager.deleteSession()
        sessionJob?.cancel()
        setSessionStatus(SessionStatus.NotAuthenticated(true))
        sessionJob = null
    }

    override suspend fun awaitInitialization() {
        sessionStatus.first { it !is SessionStatus.Initializing }
    }

    override fun setSessionStatus(status: SessionStatus) {
        Auth.logger.d { "Setting session status to $status" }
        _sessionStatus.value = status
    }

    override fun emitEvent(event: AuthEvent) {
        Auth.logger.d { "Emitting event $event" }
        _events.tryEmit(event)
    }

    /**
     * Prepares PKCE if enabled and returns the code challenge.
     */
    private fun preparePKCEIfEnabled(): String? {
        if (this.config.flowType != FlowType.PKCE) return null
        val codeVerifier = generateCodeVerifier()
        authScope.launch {
            supabaseClient.auth.codeVerifierCache.saveCodeVerifier(codeVerifier)
        }
        return generateCodeChallenge(codeVerifier)
    }

}

@SupabaseInternal
expect fun Auth.setupPlatform()

@SupabaseInternal
expect fun Auth.createDefaultSessionManager(): SessionManager

@SupabaseInternal
expect fun Auth.createDefaultCodeVerifierCache(): CodeVerifierCache
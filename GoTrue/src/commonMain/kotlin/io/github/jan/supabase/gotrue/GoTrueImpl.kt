package io.github.jan.supabase.gotrue

 import io.github.aakira.napier.Napier
 import io.github.jan.supabase.SupabaseClient
 import io.github.jan.supabase.annotiations.SupabaseExperimental
 import io.github.jan.supabase.bodyOrNull
 import io.github.jan.supabase.exceptions.BadRequestRestException
 import io.github.jan.supabase.exceptions.RestException
 import io.github.jan.supabase.exceptions.UnauthorizedRestException
 import io.github.jan.supabase.exceptions.UnknownRestException
 import io.github.jan.supabase.gotrue.admin.AdminApi
 import io.github.jan.supabase.gotrue.admin.AdminApiImpl
 import io.github.jan.supabase.gotrue.mfa.MfaApi
 import io.github.jan.supabase.gotrue.mfa.MfaApiImpl
 import io.github.jan.supabase.gotrue.providers.AuthProvider
 import io.github.jan.supabase.gotrue.providers.ExternalAuthConfigDefaults
 import io.github.jan.supabase.gotrue.providers.OAuthProvider
 import io.github.jan.supabase.gotrue.providers.builtin.DefaultAuthProvider
 import io.github.jan.supabase.gotrue.providers.builtin.SSO
 import io.github.jan.supabase.gotrue.user.UserInfo
 import io.github.jan.supabase.gotrue.user.UserSession
 import io.github.jan.supabase.putJsonObject
 import io.github.jan.supabase.safeBody
 import io.github.jan.supabase.supabaseJson
 import io.ktor.client.call.body
 import io.ktor.client.statement.HttpResponse
 import io.ktor.client.statement.bodyAsText
 import io.ktor.http.HttpStatusCode
 import kotlinx.coroutines.CoroutineScope
 import kotlinx.coroutines.Job
 import kotlinx.coroutines.cancel
 import kotlinx.coroutines.delay
 import kotlinx.coroutines.flow.MutableStateFlow
 import kotlinx.coroutines.flow.StateFlow
 import kotlinx.coroutines.flow.asStateFlow
 import kotlinx.coroutines.launch
 import kotlinx.datetime.Clock
 import kotlinx.serialization.decodeFromString
 import kotlinx.serialization.json.JsonObject
 import kotlinx.serialization.json.JsonObjectBuilder
 import kotlinx.serialization.json.buildJsonObject
 import kotlinx.serialization.json.encodeToJsonElement
 import kotlinx.serialization.json.put
 import kotlinx.serialization.json.putJsonObject
 import kotlin.time.Duration.Companion.seconds

@PublishedApi
internal class GoTrueImpl(override val supabaseClient: SupabaseClient, override val config: GoTrueConfig) : GoTrue {

    private val _sessionStatus = MutableStateFlow<SessionStatus>(SessionStatus.NotAuthenticated)
    override val sessionStatus: StateFlow<SessionStatus> = _sessionStatus.asStateFlow()
    internal val authScope = CoroutineScope(config.coroutineDispatcher)
    override val sessionManager = config.sessionManager ?: SettingsSessionManager()
    override val codeVerifierCache = config.codeVerifierCache ?: SettingsCodeVerifierCache()
    internal val api = supabaseClient.authenticatedSupabaseApi(this)
    override val admin: AdminApi = AdminApiImpl(this)
    override val mfa: MfaApi = MfaApiImpl(this)
    var sessionJob: Job? = null
    override val isAutoRefreshRunning: Boolean
        get() = sessionJob?.isActive == true

    init {
        setupPlatform()
        if(config.autoLoadFromStorage) {
            _sessionStatus.value = SessionStatus.LoadingFromStorage
            authScope.launch {
                Napier.d {
                    "Trying to load latest session"
                }
                val successful = loadFromStorage()
                if (successful) {
                    Napier.d {
                        "Successfully loaded session from storage"
                    }
                } else {
                    _sessionStatus.value = SessionStatus.NotAuthenticated
                }
            }
        }
    }

    override val API_VERSION: Int
        get() = GoTrue.API_VERSION

    override val PLUGIN_KEY: String
        get() = GoTrue.key

    @Deprecated("Use logout() instead", replaceWith = ReplaceWith("logout()"))
    override suspend fun invalidateAllRefreshTokens() {
        logout()
    }

    override suspend fun <C, R, Provider : AuthProvider<C, R>> loginWith(
        provider: Provider,
        redirectUrl: String?,
        config: (C.() -> Unit)?
    ) = provider.login(supabaseClient, {
        importSession(it)
    }, redirectUrl, config)

    override suspend fun <C, R, Provider : AuthProvider<C, R>> signUpWith(
        provider: Provider,
        redirectUrl: String?,
        config: (C.() -> Unit)?
    ): R? = provider.signUp(supabaseClient, {
        importSession(it)
    }, redirectUrl, config)

    override suspend fun <Config : SSO.Config> retrieveSSOUrl(
        type: SSO<Config>,
        redirectUrl: String?,
        config: (Config.() -> Unit)?
    ): SSO.Result {
        val createdConfig = type.config.apply { config?.invoke(this) }
        return api.postJson("sso", buildJsonObject {
            redirectUrl?.let { put("redirect_to", it) }
            createdConfig.captchaToken?.let {
                put("gotrue_meta_security", buildJsonObject {
                    put("captcha_token", it)
                })
            }
            when(createdConfig) {
                is SSO.Config.Domain -> put("domain", createdConfig.domain)
                is SSO.Config.Provider -> put("provider_id", createdConfig.providerId)
            }
        }).body()
    }

    override suspend fun <Config, Result, Provider : DefaultAuthProvider<Config, Result>> modifyUser(
        provider: Provider,
        extraData: JsonObject?,
        config: Config.() -> Unit
    ): UserInfo {
        val body = buildJsonObject {
            putJsonObject(provider.encodeCredentials(config))
            extraData?.let {
                put("data", supabaseJson.encodeToJsonElement(it))
            }
        }.toString()
        val response = api.putJson("user", body)
        return response.safeBody()
    }

    @OptIn(SupabaseExperimental::class)
    override suspend fun <C, R, Provider : DefaultAuthProvider<C, R>> sendOtpTo(
        provider: Provider,
        createUser: Boolean,
        redirectUrl: String?,
        data: JsonObject?,
        config: C.() -> Unit
    ) {
        val finalRedirectUrl = generateRedirectUrl(redirectUrl)
        val body = buildJsonObject {
            putJsonObject(provider.encodeCredentials(config))
            put("create_user", createUser)
            data?.let {
                put("data", it)
            }
        }
        var codeChallenge: String? = null
        if(this.config.flowType == FlowType.PKCE) {
            val codeVerifier = generateCodeVerifier()
            codeVerifierCache.saveCodeVerifier(codeVerifier)
            codeChallenge = generateCodeChallenge(codeVerifier)
        }
        api.postJson("otp", buildJsonObject {
            putJsonObject(body)
            codeChallenge?.let {
                put("code_challenge", it)
                put("code_challenge_method", "s256")
            }
        }) {
            finalRedirectUrl?.let { url.parameters.append("redirect_to", it) }
        }
    }

    override suspend fun sendRecoveryEmail(email: String, redirectUrl: String?, captchaToken: String?) {
        val finalRedirectUrl = generateRedirectUrl(redirectUrl)
        val body = buildJsonObject {
            put("email", email)
            captchaToken?.let {
                putJsonObject("gotrue_meta_security") {
                    put("captcha_token", captchaToken)
                }
            }
        }.toString()
        api.postJson("recover", body) {
            finalRedirectUrl?.let { url.encodedParameters.append("redirect_to", it) }
        }
    }

    override suspend fun reauthenticate() {
        api.get("reauthenticate")
    }

    override suspend fun logout() {
        sessionManager.deleteSession()
        sessionJob?.cancel()
        _sessionStatus.value = SessionStatus.NotAuthenticated
        sessionJob = null
    }

    private suspend fun verify(type: String, token: String, captchaToken: String?, additionalData: JsonObjectBuilder.() -> Unit) {
        val body = buildJsonObject {
            put("type", type)
            put("token", token)
            captchaToken?.let {
                putJsonObject("gotrue_meta_security") {
                    put("captcha_token", captchaToken)
                }
            }
            additionalData()
        }
        val response = api.postJson("verify", body)
        val session =  response.body<UserSession>()
        importSession(session)
    }

    override suspend fun verifyEmailOtp(
        type: OtpType.Email,
        email: String,
        token: String,
        captchaToken: String?
    ) = verify(type.type, token, captchaToken) {
        put("email", email)
    }

    override suspend fun verifyPhoneOtp(
        type: OtpType.Phone,
        phoneNumber: String,
        token: String,
        captchaToken: String?
    ) = verify(type.type, token, captchaToken) {
        put("phone", phoneNumber)
    }

    override suspend fun retrieveUser(jwt: String): UserInfo {
        val response = api.get("user") {
            headers["Authorization"] = "Bearer $jwt"
        }
        val body = response.bodyAsText()
        return supabaseJson.decodeFromString(body)
    }

    override suspend fun retrieveUserForCurrentSession(updateSession: Boolean): UserInfo {
        val user = retrieveUser(currentAccessTokenOrNull() ?: throw IllegalStateException("No session found"))
        if(updateSession) {
            val session = currentSessionOrNull() ?: throw IllegalStateException("No session found")
            val newStatus = SessionStatus.Authenticated(session.copy(user = user))
            _sessionStatus.value = newStatus
            if(config.autoSaveToStorage) sessionManager.saveSession(newStatus.session)
        }
        return user
    }

    override suspend fun invalidateSession() {
        sessionManager.deleteSession()
        sessionJob?.cancel()
        _sessionStatus.value = SessionStatus.NotAuthenticated
        sessionJob = null
    }

    override suspend fun exchangeCodeForSession(code: String, saveSession: Boolean): UserSession {
        val codeVerifier = codeVerifierCache.loadCodeVerifier()
        val session = api.postJson("token?grant_type=pkce", buildJsonObject {
            put("auth_code", code)
            put("code_verifier", codeVerifier)
        }) {
            headers.remove("Authorization")
        }.safeBody<UserSession>()
        codeVerifierCache.deleteCodeVerifier()
        if(saveSession) {
            importSession(session)
        }
        return session
    }

    override suspend fun refreshSession(refreshToken: String): UserSession {
        Napier.d {
            "Refreshing session"
        }
        val body = buildJsonObject {
            put("refresh_token", refreshToken)
        }
        val response = api.postJson("token?grant_type=refresh_token", body) {
            headers.remove("Authorization")
        }
        return response.safeBody("GoTrue#refreshSession")
    }

    override suspend fun refreshCurrentSession() {
        val newSession = refreshSession(currentSessionOrNull()?.refreshToken ?: throw IllegalStateException("No refresh token found in current session"))
        importSession(newSession)
    }

    override suspend fun updateCurrentUser() {
        val session = currentSessionOrNull() ?: throw IllegalStateException("No session found")
        val user = retrieveUser(session.accessToken)
        _sessionStatus.value = SessionStatus.Authenticated(session.copy(user = user))
        if(config.autoSaveToStorage) sessionManager.saveSession(session)
    }

    override suspend fun startAutoRefresh(session: UserSession, autoRefresh: Boolean) = importSession(session, autoRefresh)

    override suspend fun importSession(session: UserSession, autoRefresh: Boolean) {
        if(!autoRefresh) {
            _sessionStatus.value = SessionStatus.Authenticated(session)
            if(session.refreshToken.isNotBlank() && session.expiresIn != 0L && config.autoSaveToStorage) {
                sessionManager.saveSession(session)
            }
            return
        }
        if(session.expiresAt <= Clock.System.now()) {
            Napier.d {
                "Session expired. Refreshing session..."
            }
            try {
                val newSession = refreshSession(session.refreshToken)
                importSession(newSession, config.alwaysAutoRefresh)
            } catch(e: RestException) {
                logout()
                Napier.e(e) { "Couldn't refresh session. The refresh token may have been revoked." }
            } catch (e: Exception) {
                Napier.e(e) { "Couldn't reach supabase. Either the address doesn't exist or the network might not be on. Retrying in ${config.retryDelay}" }
                _sessionStatus.value = SessionStatus.NetworkError
                delay(config.retryDelay)
                importSession(session)
            }
        } else {
            _sessionStatus.value = SessionStatus.Authenticated(session)
            if(config.autoSaveToStorage) sessionManager.saveSession(session)
            sessionJob?.cancel()
            sessionJob = authScope.launch {
                delay(session.expiresIn.seconds.inWholeMilliseconds)
                launch {
                    Napier.d {
                        "Session expired. Refreshing session..."
                    }
                    try {
                        val newSession = refreshSession(session.refreshToken)
                        importSession(newSession)
                    } catch(e: RestException) {
                        logout()
                        Napier.e(e) { "Couldn't refresh session. The refresh token may have been revoked." }
                    } catch (e: Exception) {
                        Napier.e(e) { "Couldn't reach supabase. Either the address doesn't exist or the network might not be on. Retrying in ${config.retryDelay}" }
                        _sessionStatus.value = SessionStatus.NetworkError
                    }
                }
            }
        }
    }

    override suspend fun startAutoRefreshForCurrentSession() = importSession(currentSessionOrNull() ?: throw IllegalStateException("No session found"), true)

    override fun stopAutoRefreshForCurrentSession() {
        sessionJob?.cancel()
        sessionJob = null
    }

    override suspend fun loadFromStorage(autoRefresh: Boolean): Boolean {
        val session = sessionManager.loadSession()
        session?.let {
            importSession(it, autoRefresh)
        }
        return session != null
    }

    override suspend fun close() {
        authScope.cancel()
    }

    override suspend fun parseErrorResponse(response: HttpResponse): RestException {
        val errorBody = response.bodyOrNull<GoTrueErrorResponse>() ?: GoTrueErrorResponse("Unknown error", "")
        return when(response.status) {
            HttpStatusCode.Unauthorized -> UnauthorizedRestException(errorBody.error, response, errorBody.description)
            HttpStatusCode.BadRequest -> BadRequestRestException(errorBody.error, response, errorBody.description)
            HttpStatusCode.UnprocessableEntity -> BadRequestRestException(errorBody.error, response, errorBody.description)
            else -> UnknownRestException(errorBody.error, response)
        }
    }

    override fun oAuthUrl(
        provider: OAuthProvider,
        redirectUrl: String?,
        additionalConfig: ExternalAuthConfigDefaults.() -> Unit
    ): String {
        val config = ExternalAuthConfigDefaults().apply(additionalConfig)
        return resolveUrl(buildString {
            append("authorize?provider=${provider.name}&redirect_to=$redirectUrl")
            if(config.scopes.isNotEmpty()) append("&scopes=${config.scopes.joinToString("+")}")
            if(config.queryParams.isNotEmpty()) {
                for((key, value) in config.queryParams) {
                    append("&$key=$value")
                }
            }
        })
    }

}

expect fun GoTrue.setupPlatform()
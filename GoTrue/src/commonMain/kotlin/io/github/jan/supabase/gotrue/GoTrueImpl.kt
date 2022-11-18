package io.github.jan.supabase.gotrue

 import io.github.aakira.napier.Napier
 import io.github.jan.supabase.SupabaseClient
 import io.github.jan.supabase.exceptions.BadRequestRestException
 import io.github.jan.supabase.exceptions.RestException
 import io.github.jan.supabase.exceptions.UnauthorizedRestException
 import io.github.jan.supabase.exceptions.UnknownRestException
 import io.github.jan.supabase.gotrue.admin.AdminApi
 import io.github.jan.supabase.gotrue.admin.AdminApiImpl
 import io.github.jan.supabase.gotrue.providers.AuthProvider
 import io.github.jan.supabase.gotrue.providers.builtin.DefaultAuthProvider
 import io.github.jan.supabase.gotrue.user.UserInfo
 import io.github.jan.supabase.gotrue.user.UserSession
 import io.github.jan.supabase.putJsonObject
 import io.github.jan.supabase.supabaseJson
 import io.github.jan.supabase.toJsonObject
 import io.ktor.client.call.body
 import io.ktor.client.request.header
 import io.ktor.client.statement.bodyAsText
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
 import kotlinx.serialization.json.buildJsonObject
 import kotlinx.serialization.json.encodeToJsonElement
 import kotlinx.serialization.json.put
 import kotlinx.serialization.json.putJsonObject
 import kotlin.time.Duration.Companion.seconds

@PublishedApi
internal class GoTrueImpl(override val supabaseClient: SupabaseClient, override val config: GoTrue.Config) : GoTrue {

    private val _sessionStatus = MutableStateFlow<SessionStatus>(SessionStatus.NotAuthenticated)
    override val sessionStatus: StateFlow<SessionStatus> = _sessionStatus.asStateFlow()
    private val authScope = CoroutineScope(config.coroutineDispatcher)
    override val sessionManager = config.sessionManager ?: SettingsSessionManager()
    internal val api = supabaseClient.authenticatedSupabaseApi(this)
    override val admin: AdminApi = AdminApiImpl(this)
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

    override suspend fun invalidateAllRefreshTokens() {
        api.post("logout")
        invalidateSession()
    }

    override suspend fun <C, R, Provider : AuthProvider<C, R>> loginWith(
        provider: Provider,
        redirectUrl: String?,
        config: (C.() -> Unit)?
    ) = provider.login(supabaseClient, {
        startAutoRefresh(it)
    }, redirectUrl, config)

    override suspend fun <C, R, Provider : AuthProvider<C, R>> signUpWith(
        provider: Provider,
        redirectUrl: String?,
        config: (C.() -> Unit)?
    ): R = provider.signUp(supabaseClient, {
        startAutoRefresh(it)
    }, redirectUrl, config)

    override suspend fun <Config, Result, Provider : DefaultAuthProvider<Config, Result>> modifyUser(
        provider: Provider,
        extraData: JsonObject?,
        config: Config.() -> Unit
    ): UserInfo {
        val body = buildJsonObject {
            putJsonObject(provider.encodeCredentials(config).toJsonObject())
            extraData?.let {
                put("data", supabaseJson.encodeToJsonElement(it))
            }
        }.toString()
        val response = api.putJson("user", body)
        return response.body()
    }

    override suspend fun <C, R, Provider : DefaultAuthProvider<C, R>> sendOtpTo(
        provider: Provider,
        createUser: Boolean,
        redirectUrl: String?,
        config: C.() -> Unit
    ) {
        val finalRedirectUrl = generateRedirectUrl(redirectUrl)
        val body = buildJsonObject {
            putJsonObject(provider.encodeCredentials(config).toJsonObject())
            put("create_user", createUser)
        }.toString()
        api.postJson("otp", body) {
            finalRedirectUrl?.let { url.parameters.append("redirect_to", it) }
        }.checkErrors()
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
        }.checkErrors()
    }

    override suspend fun reauthenticate() {
        api.get("reauthenticate").checkErrors()
    }

    override suspend fun verify(type: VerifyType, token: String, captchaToken: String?) {
        val body = buildJsonObject {
            put("type", type.name.lowercase())
            put("token", token)
            captchaToken?.let {
                putJsonObject("gotrue_meta_security") {
                    put("captcha_token", captchaToken)
                }
            }
        }
        val response = api.postJson("verify", body)
        val session =  response.checkErrors().body<UserSession>()
        startAutoRefresh(session)
    }

    override suspend fun verifyPhone(token: String, phoneNumber: String, captchaToken: String?) {
        val body = buildJsonObject {
            put("type", "sms")
            put("token", token)
            put("phone", phoneNumber)
            captchaToken?.let {
                putJsonObject("gotrue_meta_security") {
                    put("captcha_token", captchaToken)
                }
            }
        }
        val response = api.postJson("verify", body)
        val session = response.checkErrors().body<UserSession>()
        startAutoRefresh(session)
    }

    override suspend fun getUser(jwt: String): UserInfo {
        val response = api.get("user") {
            header("Authorization", "Bearer $jwt")
        }
        val body = response.bodyAsText()
        return supabaseJson.decodeFromString(body)
    }

    override suspend fun invalidateSession() {
        sessionManager.deleteSession()
        sessionJob?.cancel()
        _sessionStatus.value = SessionStatus.NotAuthenticated
        sessionJob = null
    }

    override suspend fun refreshSession(refreshToken: String): UserSession {
        Napier.d {
            "Refreshing session"
        }
        val body = buildJsonObject {
            put("refresh_token", refreshToken)
        }
        val response = api.postJson("token?grant_type=refresh_token", body)
        return response.body()
    }

    override suspend fun refreshCurrentSession() {
        val newSession = refreshSession(currentAccessTokenOrNull() ?: throw IllegalStateException("No refresh token found in current session"))
        startAutoRefresh(newSession)
    }

    override suspend fun startAutoRefresh(session: UserSession, autoRefresh: Boolean) {
        if(!autoRefresh) {
            _sessionStatus.value = SessionStatus.Authenticated(session)
            if(session.refreshToken.isNotBlank() && session.expiresIn != 0L) {
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
                startAutoRefresh(newSession, config.alwaysAutoRefresh)
            } catch(e: RestException) {
                invalidateSession()
                Napier.e(e) { "Couldn't refresh session. The refresh token may have been revoked." }
            } catch (e: Exception) {
                Napier.e(e) { "Couldn't reach supabase. Either the address doesn't exist or the network might not be on. Retrying in ${config.retryDelay}" }
                _sessionStatus.value = SessionStatus.NetworkError
                delay(config.retryDelay)
                startAutoRefresh(session)
            }
        } else {
            _sessionStatus.value = SessionStatus.Authenticated(session)
            sessionManager.saveSession(session)
            sessionJob?.cancel()
            sessionJob = authScope.launch {
                delay(session.expiresIn.seconds.inWholeMilliseconds)
                launch {
                    Napier.d {
                        "Session expired. Refreshing session..."
                    }
                    try {
                        val newSession = refreshSession(session.refreshToken)
                        startAutoRefresh(newSession)
                    } catch(e: RestException) {
                        invalidateSession()
                        Napier.e(e) { "Couldn't refresh session. The refresh token may have been revoked." }
                    } catch (e: Exception) {
                        Napier.e(e) { "Couldn't reach supabase. Either the address doesn't exist or the network might not be on. Retrying in ${config.retryDelay}" }
                        _sessionStatus.value = SessionStatus.NetworkError
                    }
                }
            }
        }
    }

    override suspend fun startAutoRefreshForCurrentSession() = startAutoRefresh(currentSessionOrNull() ?: throw IllegalStateException("No session found"), true)

    override fun stopAutoRefreshForCurrentSession() {
        sessionJob?.cancel()
        sessionJob = null
    }

    override suspend fun importSession(session: UserSession, autoRefresh: Boolean) = startAutoRefresh(session, autoRefresh)

    override suspend fun loadFromStorage(autoRefresh: Boolean): Boolean {
        val session = sessionManager.loadSession()
        val wasSuccessful = session != null
        if(wasSuccessful) startAutoRefresh(session!!, autoRefresh)
        return wasSuccessful
    }

    override suspend fun close() {
        authScope.cancel()
    }

    override suspend fun parseErrorResponse(response: HttpResponse): RestException {
        val errorCode = response.status.value
        val errorBody = response.body<GoTrueErrorResponse>()
        return when(errorCode) {
            401 -> UnauthorizedRestException(errorBody.error, response)
            400 -> BadRequestRestException(errorBody.error, response)
            422 -> BadRequestRestException(errorBody.error, response)
            else -> UnknownRestException(errorBody.error, response)
        }
    }

}

expect fun GoTrue.setupPlatform()
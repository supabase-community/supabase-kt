package io.github.jan.supabase.gotrue

 import io.github.aakira.napier.Napier
 import io.github.jan.supabase.SupabaseClient
 import io.github.jan.supabase.exceptions.RestException
 import io.github.jan.supabase.exceptions.UnauthorizedException
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
 import io.ktor.client.request.HttpRequestBuilder
 import io.ktor.client.request.get
 import io.ktor.client.request.header
 import io.ktor.client.request.headers
 import io.ktor.client.request.post
 import io.ktor.client.request.put
 import io.ktor.client.request.setBody
 import io.ktor.client.statement.bodyAsText
 import io.ktor.http.ContentType
 import io.ktor.http.HttpHeaders
 import io.ktor.http.contentType
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
        supabaseClient.httpClient.post(resolveUrl("logout")) {
            addAuthorization()
        }
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
        val response = supabaseClient.httpClient.put(resolveUrl("user")) {
            addAuthorization()
            setBody(body)
        }
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
        val redirect = finalRedirectUrl?.let {
            "?redirect_to=$finalRedirectUrl"
        } ?: ""
        supabaseClient.httpClient.post(resolveUrl("otp$redirect")) {
            setBody(body)
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
        val redirect = finalRedirectUrl?.let {
            "?redirect_to=$finalRedirectUrl"
        } ?: ""
        supabaseClient.httpClient.post(resolveUrl("recover$redirect")) {
            setBody(body)
        }
    }

    override suspend fun reauthenticate() {
        supabaseClient.httpClient.get(resolveUrl("reauthenticate")) {
            addAuthorization()
        }
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
        val response = supabaseClient.httpClient.post(resolveUrl("verify")) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
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
        val response = supabaseClient.httpClient.post(resolveUrl("verify")) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val session = response.checkErrors().body<UserSession>()
        startAutoRefresh(session)
    }

    override suspend fun getUser(jwt: String): UserInfo {
        val response = supabaseClient.httpClient.get(resolveUrl("user")) {
            header(HttpHeaders.Authorization, "Bearer $jwt")
        }
        val body = response.bodyAsText()
        return try {
            supabaseJson.decodeFromString(body)
        } catch(e: Exception) {
            Napier.e(e) { "Failed to get user. Full response body: $body" }
            throw UnauthorizedException("Invalid JWT")
        }
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
        val body = """
            {
              "refresh_token": "$refreshToken"
            }
        """.trimIndent()
        val response = supabaseClient.httpClient.post(resolveUrl("token?grant_type=refresh_token")) {
            setBody(body)
        }
        if (response.status.value !in 200..299) throw RestException(
            response.status.value,
            "Unauthorized",
            response.bodyAsText()
        )
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

    private fun HttpRequestBuilder.addAuthorization() {
        headers {
            append(HttpHeaders.Authorization, "Bearer ${(sessionStatus.value as? SessionStatus.Authenticated)?.session?.accessToken}")
        }
    }

    override suspend fun close() {
        authScope.cancel()
    }

    private operator fun UserSession?.not(): UserSession {
        return this ?: throw IllegalStateException("No user session available")
    }

}

expect fun GoTrue.setupPlatform()
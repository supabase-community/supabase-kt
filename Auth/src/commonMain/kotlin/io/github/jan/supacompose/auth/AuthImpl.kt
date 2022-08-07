package io.github.jan.supacompose.auth

 import io.github.aakira.napier.DebugAntilog
 import io.github.aakira.napier.Napier
 import io.github.jan.supacompose.CurrentPlatformTarget
 import io.github.jan.supacompose.PlatformTarget
 import io.github.jan.supacompose.SupabaseClient
 import io.github.jan.supacompose.annotiations.SupaComposeInternal
 import io.github.jan.supacompose.auth.providers.AuthProvider
 import io.github.jan.supacompose.auth.providers.DefaultAuthProvider
 import io.github.jan.supacompose.auth.user.UserInfo
 import io.github.jan.supacompose.auth.user.UserSession
 import io.github.jan.supacompose.exceptions.RestException
 import io.github.jan.supacompose.putJsonObject
 import io.github.jan.supacompose.supabaseJson
 import io.github.jan.supacompose.toJsonObject
 import io.ktor.client.call.body
 import io.ktor.client.request.HttpRequestBuilder
 import io.ktor.client.request.get
 import io.ktor.client.request.header
 import io.ktor.client.request.headers
 import io.ktor.client.request.post
 import io.ktor.client.request.put
 import io.ktor.client.request.setBody
 import io.ktor.http.HttpHeaders
 import io.ktor.http.HttpStatusCode.Companion.Unauthorized
 import kotlinx.coroutines.CoroutineScope
 import kotlinx.coroutines.Dispatchers
 import kotlinx.coroutines.Job
 import kotlinx.coroutines.cancel
 import kotlinx.coroutines.coroutineScope
 import kotlinx.coroutines.delay
 import kotlinx.coroutines.flow.MutableStateFlow
 import kotlinx.coroutines.flow.StateFlow
 import kotlinx.coroutines.flow.asStateFlow
 import kotlinx.coroutines.launch
 import kotlinx.datetime.Clock
 import kotlinx.serialization.json.JsonObject
 import kotlinx.serialization.json.buildJsonObject
 import kotlinx.serialization.json.encodeToJsonElement
 import kotlinx.serialization.json.put
 import kotlin.time.Duration.Companion.seconds

@PublishedApi
internal class AuthImpl(override val supabaseClient: SupabaseClient, override val config: Auth.Config) : Auth {

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    override val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()
    private val callbacks = mutableListOf<(new: UserSession?, old: UserSession?) -> Unit>()
    private val authScope = CoroutineScope(Dispatchers.Default + Job())
    override val sessionManager = SessionManager()
    val _status = MutableStateFlow(Auth.Status.NOT_AUTHENTICATED)
    override val status = _status.asStateFlow()
    var sessionJob: Job? = null

    init {
        Napier.base(DebugAntilog())
        if (CurrentPlatformTarget == PlatformTarget.WEB || CurrentPlatformTarget == PlatformTarget.DESKTOP) { //for android see Android.kt
            authScope.launch {
                Napier.d {
                    "Trying to load latest session"
                }
                _status.value = Auth.Status.LOADING_FROM_STORAGE
                val session = sessionManager.loadSession(supabaseClient, this@AuthImpl)
                if (session != null) {
                    Napier.d {
                        "Successfully loaded session from storage"
                    }
                    startJob(session)
                } else {
                    _status.value = Auth.Status.NOT_AUTHENTICATED
                }
            }
        }
    }

    override suspend fun invalidateAllRefreshTokens() {
        supabaseClient.httpClient.post(path("logout")) {
            addAuthorization()
        }
        invalidateSession()
    }

    override suspend fun <Config, Result, Provider : AuthProvider<Config, Result>> loginWith(
        provider: Provider,
        redirectUrl: String?,
        config: (Config.() -> Unit)?
    ) = provider.login(supabaseClient, {
        startJob(it)
    }, redirectUrl, config)

    override suspend fun <Config, Result, Provider : AuthProvider<Config, Result>> signUpWith(
        provider: Provider,
        redirectUrl: String?,
        config: (Config.() -> Unit)?
    ) = provider.signUp(supabaseClient, {
        startJob(it)
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
        val response = supabaseClient.httpClient.put(path("user")) {
            addAuthorization()
            setBody(body)
        }
        return response.body()
    }

    override suspend fun <Config, Result, Provider : DefaultAuthProvider<Config, Result>> sendOtpTo(
        provider: Provider,
        createUser: Boolean,
        redirectUrl: String?,
        config: Config.() -> Unit
    ) {
        val finalRedirectUrl = generateRedirectUrl(redirectUrl)
        val body = buildJsonObject {
            putJsonObject(provider.encodeCredentials(config).toJsonObject())
            put("create_user", createUser)
        }.toString()
        val redirect = finalRedirectUrl?.let {
            "?redirect_to=$finalRedirectUrl"
        } ?: ""
        supabaseClient.httpClient.post(path("otp$redirect")) {
            setBody(body)
        }
    }

    override suspend fun sendRecoveryEmail(email: String, redirectUrl: String?) {
        val finalRedirectUrl = generateRedirectUrl(redirectUrl)
        val body = buildJsonObject {
            put("email", email)
        }.toString()
        val redirect = finalRedirectUrl?.let {
            "?redirect_to=$finalRedirectUrl"
        } ?: ""
        supabaseClient.httpClient.post(path("recover$redirect")) {
            setBody(body)
        }
    }

    override suspend fun reauthenticate() {
        supabaseClient.httpClient.get(path("reauthenticate")) {
            addAuthorization()
        }
    }

    override suspend fun verify(type: VerifyType, token: String) {
        val body = """
            {
              "type": "${type.name.lowercase()}",
              "token": "$token"
            }
        """.trimIndent()
        val response = supabaseClient.httpClient.post(path("verify")) {
            setBody(body)
            addAuthorization()
        }
        val session =  response.body<UserSession>()
        startJob(session)
    }

    override suspend fun getUser(jwt: String): UserInfo {
        val response = supabaseClient.httpClient.get(path("user")) {
            header(HttpHeaders.Authorization, "Bearer $jwt")
        }
        return response.body()
    }

    override suspend fun invalidateSession() {
        sessionManager.deleteSession(supabaseClient, this)
        sessionJob?.cancel()
        updateSession(Auth.Status.NOT_AUTHENTICATED, null)
        sessionJob = null
    }

    private suspend fun refreshSession(refreshToken: String) {
        Napier.d {
            "Refreshing session"
        }
        val body = """
            {
              "refresh_token": "$refreshToken"
            }
        """.trimIndent()
        val response = supabaseClient.httpClient.post(path("token?grant_type=refresh_token")) {
            setBody(body)
        }
        if(response.status.value !in 200..299) throw RestException(401, "Unauthorized", "Refresh token is invalid")
        val newSession =  response.body<UserSession>()
        startJob(newSession)
    }

    internal suspend fun startJob(session: UserSession, autoRefresh: Boolean = config.alwaysAutoRefresh) {
        Napier.d {
            "(Re)starting session job"
        }
        if(!autoRefresh) {
            updateSession(Auth.Status.AUTHENTICATED, session)
            if(session.refreshToken.isNotBlank() && session.expiresIn != 0L) {
                sessionManager.saveSession(supabaseClient, this, session)
            }
            return
        }
        if(session.expiresAt < Clock.System.now()) {
            try {
                refreshSession(session.refreshToken)
            } catch(e: RestException) {
                invalidateSession()
                Napier.e(e) { "Couldn't refresh session. The refresh token may have been revoked." }
            } catch (e: Exception) {
                Napier.e(e) { "Couldn't reach supabase. Either the address doesn't exist or the network might not be on. Retrying in ${config.retryDelay}" }
                _status.value = Auth.Status.NETWORK_ERROR
                delay(config.retryDelay)
                startJob(session)
            }
        } else {
            updateSession(Auth.Status.AUTHENTICATED, session)
            sessionManager.saveSession(supabaseClient, this, session)
            coroutineScope {
                sessionJob = launch {
                    delay(session.expiresIn.seconds.inWholeMilliseconds)
                    launch {
                        try {
                            refreshSession(session.refreshToken)
                        } catch(e: RestException) {
                            invalidateSession()
                            Napier.e(e) { "Couldn't refresh session. The refresh token may have been revoked." }
                        } catch (e: Exception) {
                            Napier.e(e) { "Couldn't reach supabase. Either the address doesn't exist or the network might not be on. Retrying in ${config.retryDelay}" }
                            _status.value = Auth.Status.NETWORK_ERROR
                            coroutineScope {
                                launch {
                                    delay(config.retryDelay)
                                    startJob(session)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override suspend fun importSession(session: UserSession, autoRefresh: Boolean) = startJob(session, autoRefresh)

    override fun onSessionChange(callback: (new: UserSession?, old: UserSession?) -> Unit) {
        callbacks += callback
    }

    private fun HttpRequestBuilder.addAuthorization() {
        headers {
            append(HttpHeaders.Authorization, "Bearer ${(!currentSession.value).accessToken}")
        }
    }

    private fun updateSession(status: Auth.Status, newSession: UserSession?) {
        _status.value = status
        val oldSession = currentSession.value
        _currentSession.value = newSession
        callbacks.forEach { it.invoke(newSession, oldSession) }
    }

    @OptIn(SupaComposeInternal::class)
    override fun path(path: String) = supabaseClient.path("auth/v${Auth.API_VERSION}/$path")

    override suspend fun close() {
        authScope.cancel()
    }

    private operator fun UserSession?.not(): UserSession {
        return this ?: throw IllegalStateException("No user session available")
    }

}
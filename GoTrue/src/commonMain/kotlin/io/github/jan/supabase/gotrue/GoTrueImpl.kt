package io.github.jan.supabase.gotrue

 import io.github.aakira.napier.Napier
 import io.github.jan.supabase.SupabaseClient
 import io.github.jan.supabase.gotrue.admin.AdminApi
 import io.github.jan.supabase.gotrue.admin.AdminApiImpl
 import io.github.jan.supabase.gotrue.providers.AuthProvider
 import io.github.jan.supabase.gotrue.providers.builtin.DefaultAuthProvider
 import io.github.jan.supabase.gotrue.user.UserInfo
 import io.github.jan.supabase.gotrue.user.UserSession
 import io.github.jan.supabase.exceptions.RestException
 import io.github.jan.supabase.exceptions.UnauthorizedException
 import io.github.jan.supabase.putJsonObject
 import io.github.jan.supabase.supabaseJson
 import io.github.jan.supabase.toJsonObject
 import io.ktor.client.call.NoTransformationFoundException
 import io.ktor.client.call.body
 import io.ktor.client.request.HttpRequestBuilder
 import io.ktor.client.request.get
 import io.ktor.client.request.header
 import io.ktor.client.request.headers
 import io.ktor.client.request.post
 import io.ktor.client.request.put
 import io.ktor.client.request.setBody
 import io.ktor.client.statement.bodyAsText
 import io.ktor.http.HttpHeaders
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
internal class GoTrueImpl(override val supabaseClient: SupabaseClient, override val config: GoTrue.Config) : GoTrue {

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    override val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()
    private val authScope = CoroutineScope(config.coroutineDispatcher)
    override val sessionManager = config.sessionManager
    override val admin: AdminApi = AdminApiImpl(this)
    val _status = MutableStateFlow(GoTrue.Status.NOT_AUTHENTICATED)
    override val status = _status.asStateFlow()
    var sessionJob: Job? = null
    override val isAutoRefreshRunning: Boolean
        get() = sessionJob?.isActive == true

    init {
        setupPlatform()
        if(config.autoLoadFromStorage) {
            _status.value = GoTrue.Status.LOADING_FROM_STORAGE
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
                    _status.value = GoTrue.Status.NOT_AUTHENTICATED
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
        val response = supabaseClient.httpClient.put(resolveUrl("user")) {
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
        supabaseClient.httpClient.post(resolveUrl("otp$redirect")) {
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
        supabaseClient.httpClient.post(resolveUrl("recover$redirect")) {
            setBody(body)
        }
    }

    override suspend fun reauthenticate() {
        supabaseClient.httpClient.get(resolveUrl("reauthenticate")) {
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
        val response = supabaseClient.httpClient.post(resolveUrl("verify")) {
            setBody(body)
            addAuthorization()
        }
        val session =  response.body<UserSession>()
        startJob(session)
    }

    override suspend fun getUser(jwt: String): UserInfo {
        val response = supabaseClient.httpClient.get(resolveUrl("user")) {
            header(HttpHeaders.Authorization, "Bearer $jwt")
        }
        return try {
            response.body()
        } catch(e: NoTransformationFoundException) {
            throw UnauthorizedException("Invalid JWT")
        }
    }

    override suspend fun invalidateSession() {
        sessionManager.deleteSession()
        sessionJob?.cancel()
        updateSession(GoTrue.Status.NOT_AUTHENTICATED, null)
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
        val response = supabaseClient.httpClient.post(resolveUrl("token?grant_type=refresh_token")) {
            setBody(body)
        }
        if(response.status.value !in 200..299) throw RestException(response.status.value, "Unauthorized", response.bodyAsText())
        val newSession =  response.body<UserSession>()
        startJob(newSession)
    }

    internal suspend fun startJob(session: UserSession, autoRefresh: Boolean = config.alwaysAutoRefresh) {
        if(!autoRefresh) {
            updateSession(GoTrue.Status.AUTHENTICATED, session)
            if(session.refreshToken.isNotBlank() && session.expiresIn != 0L) {
                sessionManager.saveSession(session)
            }
            return
        }
        if(session.expiresAt < Clock.System.now()) {
            Napier.d {
                "(Re)starting session job"
            }
            try {
                println(session.refreshToken)
                refreshSession(session.refreshToken)
            } catch(e: RestException) {
                invalidateSession()
                Napier.e(e) { "Couldn't refresh session. The refresh token may have been revoked." }
            } catch (e: Exception) {
                Napier.e(e) { "Couldn't reach supabase. Either the address doesn't exist or the network might not be on. Retrying in ${config.retryDelay}" }
                _status.value = GoTrue.Status.NETWORK_ERROR
                delay(config.retryDelay)
                startJob(session)
            }
        } else {
            updateSession(GoTrue.Status.AUTHENTICATED, session)
            sessionManager.saveSession(session)
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
                            _status.value = GoTrue.Status.NETWORK_ERROR
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

    override suspend fun loadFromStorage(autoRefresh: Boolean): Boolean {
        val session = sessionManager.loadSession()
        val wasSuccessful = session != null
        if(wasSuccessful) startJob(session!!, autoRefresh)
        return wasSuccessful
    }

    private fun HttpRequestBuilder.addAuthorization() {
        headers {
            append(HttpHeaders.Authorization, "Bearer ${(!currentSession.value).accessToken}")
        }
    }

    private fun updateSession(status: GoTrue.Status, newSession: UserSession?) {
        _status.value = status
        _currentSession.value = newSession
    }

    override suspend fun close() {
        authScope.cancel()
    }

    private operator fun UserSession?.not(): UserSession {
        return this ?: throw IllegalStateException("No user session available")
    }

}

expect fun GoTrue.setupPlatform()
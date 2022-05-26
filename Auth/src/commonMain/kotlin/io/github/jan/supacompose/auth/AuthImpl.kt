package io.github.jan.supacompose.auth

import io.github.jan.supacompose.CurrentPlatformTarget
import io.github.jan.supacompose.PlatformTarget
import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.providers.AuthFail
import io.github.jan.supacompose.auth.providers.AuthProvider
import io.github.jan.supacompose.auth.providers.DefaultAuthProvider
import io.github.jan.supacompose.auth.user.UserInfo
import io.github.jan.supacompose.auth.user.UserSession
import io.github.jan.supacompose.putJsonObject
import io.github.jan.supacompose.supabaseJson
import io.github.jan.supacompose.toJsonObject
import io.ktor.client.call.body
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import kotlin.time.Duration.Companion.seconds

@PublishedApi
internal class AuthImpl(override val supabaseClient: SupabaseClient, val config: Auth.Config) : Auth {

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    override val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()
    override val sessionManager = SessionManager()
    private var sessionJob: Job? = null

    init {
        if (CurrentPlatformTarget == PlatformTarget.WEB || CurrentPlatformTarget == PlatformTarget.DESKTOP) {
            supabaseClient.launch {
                val session = sessionManager.loadSession(supabaseClient)
                if (session != null) {
                    startJob(session)
                }
            }
        }
    }

    override suspend fun logout() {
        supabaseClient.makeRequest(HttpMethod.Post, "/auth/v${Auth.API_VERSION}/logout", Headers.build {
            append("Authorization", "Bearer $${(!currentSession.value).accessToken}")
        })
        sessionManager.deleteSession(supabaseClient)
        sessionJob?.cancel()
        _currentSession.value = null
        sessionJob = null
    }

    override suspend fun <C, R, Provider : AuthProvider<C, R>> loginWith(
        provider: Provider,
        redirectUrl: String?,
        config: (C.() -> Unit)?
    ) = provider.login(supabaseClient, {
        startJob(it)
    }, redirectUrl, config)

    override suspend fun <C, R, Provider : AuthProvider<C, R>> signUpWith(
        provider: Provider,
        redirectUrl: String?,
        config: (C.() -> Unit)?
    ) = provider.signUp(supabaseClient, {
        startJob(it)
    }, redirectUrl, config)

    override suspend fun <C, R, Provider : DefaultAuthProvider<C, R>> modifyUser(
        provider: Provider,
        extraData: JsonObject?,
        config: C.() -> Unit
    ): UserInfo {
        val body = buildJsonObject {
            putJsonObject(provider.encodeCredentials(config).toJsonObject())
            extraData?.let {
                put("data", supabaseJson.encodeToJsonElement(it))
            }
        }.toString()
        val response = supabaseClient.makeRequest(HttpMethod.Put, "/auth/v${Auth.API_VERSION}/user", Headers.build {
            append("Authorization", "Bearer ${(!currentSession.value).accessToken}")
        }, body)
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
        supabaseClient.makeRequest(HttpMethod.Post, "/auth/v${Auth.API_VERSION}/otp$redirect", body = body)
    }

    override suspend fun sendRecoveryEmail(email: String, redirectUrl: String?) {
        val finalRedirectUrl = generateRedirectUrl(redirectUrl)
        val body = buildJsonObject {
            put("email", email)
        }.toString()
        val redirect = finalRedirectUrl?.let {
            "?redirect_to=$finalRedirectUrl"
        } ?: ""
        supabaseClient.makeRequest(HttpMethod.Post, "/auth/v${Auth.API_VERSION}/recover$redirect", body = body)
    }

    override suspend fun reauthenticate() {
        supabaseClient.makeRequest(HttpMethod.Get, "/auth/v${Auth.API_VERSION}/reauthenticate", Headers.build {
            append("Authorization", "Bearer ${(!currentSession.value).accessToken}")
        })
    }

    override suspend fun verify(type: VerifyType, token: String) {
        val body = """
            {
              "type": "${type.name.lowercase()}",
              "token": "$token"
            }
        """.trimIndent()
        val response = supabaseClient.makeRequest(HttpMethod.Post, "/auth/v${Auth.API_VERSION}/verify", body = body)
        val session =  response.body<UserSession>()
        startJob(session)
    }

    override suspend fun getUser(jwt: String): UserInfo {
        val response = supabaseClient.makeRequest(HttpMethod.Get, "/auth/v${Auth.API_VERSION}/user", Headers.build {
            append("Authorization", "Bearer $jwt")
        })
        return response.body()
    }

    private suspend fun refreshSession() {
        val body = """
            {
              "refresh_token": "${(!currentSession.value).refreshToken}"
            }
        """.trimIndent()
        val response = supabaseClient.makeRequest(HttpMethod.Post, "/auth/v${Auth.API_VERSION}/token?grant_type=refresh_token", body = body)
        val newSession =  response.body<UserSession>()
        startJob(newSession)
    }

    internal suspend fun startJob(session: UserSession) {
        _currentSession.value = session
        if(session.expiresAt < Clock.System.now()) {
            refreshSession()
        } else {
            sessionManager.saveSession(supabaseClient, session)
            coroutineScope {
                sessionJob = launch {
                    delay(session.expiresIn.seconds.inWholeMilliseconds)
                    launch {
                        refreshSession()
                    }
                }
            }
        }
    }

    private operator fun UserSession?.not(): UserSession {
        return this ?: throw IllegalStateException("No user session available")
    }

}
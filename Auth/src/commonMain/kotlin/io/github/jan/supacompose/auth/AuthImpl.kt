package io.github.jan.supacompose.auth

import com.soywiz.klock.seconds
import com.soywiz.korio.async.delay
import com.soywiz.korio.async.launch
import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.gotrue.GoTrueClient
import io.github.jan.supacompose.auth.gotrue.VerifyType
import io.github.jan.supacompose.auth.providers.AuthProvider
import io.github.jan.supacompose.auth.providers.DefaultAuthProvider
import io.github.jan.supacompose.auth.providers.AuthFail
import io.github.jan.supacompose.auth.user.UserInfo
import io.github.jan.supacompose.auth.user.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.CoroutineContext

@PublishedApi
internal class AuthImpl(supabaseClient: SupabaseClient, private val config: Auth.Config) : Auth {

    override val goTrueClient = GoTrueClient.create(supabaseClient)
    private val _currentSession = MutableStateFlow<UserSession?>(null)
    override val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()
    private var sessionJob: Job? = null

    override suspend fun logout() {
        goTrueClient.logout((!currentSession.value).accessToken)
        sessionJob?.cancel()
        _currentSession.value = null
        sessionJob = null
    }

    override suspend fun <C, R, Provider : AuthProvider<C, R>> loginWith(
        provider: Provider,
        onFail: (AuthFail) -> Unit,
        config: (C.() -> Unit)?
    ) = goTrueClient.loginWith(provider, {
        startJob(it)
    } , onFail, config)

    override suspend fun <C, R, Provider : AuthProvider<C, R>> signUpWith(
        provider: Provider,
        onFail: (AuthFail) -> Unit,
        config: (C.() -> Unit)?
    ) = goTrueClient.signUpWith(provider, {
        startJob(it)
    }, onFail, config)

    override suspend fun <C, R, Provider : DefaultAuthProvider<C, R>> modifyUser(
        provider: Provider,
        config: C.() -> Unit
    ) = goTrueClient.modifyUser(provider, (!currentSession.value).accessToken, config)

    override suspend fun <C, R, Provider : DefaultAuthProvider<C, R>> sendOtpTo(
        provider: Provider,
        createUser: Boolean,
        config: C.() -> Unit
    ) = goTrueClient.sendOtpTo(provider, createUser, config)

    override suspend fun sendRecoveryEmail(email: String) = goTrueClient.sendRecoveryEmail(email)

    override suspend fun reauthenticate() = goTrueClient.reauthenticate((!currentSession.value).accessToken)

    override suspend fun verify(type: VerifyType, token: String) = goTrueClient.verify(type, token).also {
        startJob(it)
    }

    override suspend fun getUser(): UserInfo {
        return goTrueClient.getUser((!currentSession.value).accessToken)
    }

    private suspend fun refreshSession() {
        val newSession = goTrueClient.refreshSession((!currentSession.value).refreshToken)
        startJob(newSession)
    }

    internal suspend fun startJob(session: UserSession) {
        _currentSession.value = session
        coroutineScope {
            sessionJob = launch {
                delay(session.expiresIn.seconds)
                launch {
                    refreshSession()
                }
            }
        }
    }

    private operator fun UserSession?.not(): UserSession {
        return this ?: throw IllegalStateException("No user session available")
    }

}
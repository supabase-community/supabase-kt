package io.github.jan.supacompose.auth

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.gotrue.GoTrueClient
import io.github.jan.supacompose.auth.gotrue.VerifyType
import io.github.jan.supacompose.auth.providers.AuthProvider
import io.github.jan.supacompose.auth.providers.DefaultAuthProvider
import io.github.jan.supacompose.auth.providers.AuthFail
import io.github.jan.supacompose.auth.user.UserInfo
import io.github.jan.supacompose.auth.user.UserSession
import io.github.jan.supacompose.plugins.SupabasePlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

sealed interface Auth {

    val currentSession: StateFlow<UserSession?>
    val goTrueClient: GoTrueClient

    /**
     * Signs up a new user with the specified [provider]
     */
    suspend fun <C, R, Provider : AuthProvider<C, R>> signUpWith(
        provider: Provider,
        onFail: (AuthFail) -> Unit = {},
        config: (C.() -> Unit)? = null
    ): R

    /**
     * Logins the user with the specified [provider]
     */
    suspend fun <C, R, Provider : AuthProvider<C, R>> loginWith(
        provider: Provider,
        onFail: (AuthFail) -> Unit = {},
        config: (C.() -> Unit)? = null
    )

    /**
     * Modifies a user with the specified [provider]. Extra data can be supplied
     * @param provider The provider to use
     * @param config The configuration to use
     */
    suspend fun <C, R, Provider : DefaultAuthProvider<C, R>> modifyUser(
        provider: Provider,
        config: C.() -> Unit = {}
    ): UserInfo

    /**
     * Sends a one time password to the specified [provider]
     */
    suspend fun <C, R, Provider : DefaultAuthProvider<C, R>> sendOtpTo(provider: Provider, createUser: Boolean = false, config: C.() -> Unit)

    /**
     * Sends a password reset email to the user with the specified [email]
     */
    suspend fun sendRecoveryEmail(email: String)

    /**
     * Sends a nonce to the user's email (preferred) or phone
     */
    suspend fun reauthenticate()

    /**
     * Revokes all refresh tokens for the user
     */
    suspend fun logout()

    /**
     * Verifies a registration, invite or password recovery
     */
    suspend fun verify(type: VerifyType, token: String): UserSession

    /**
     * Retrieves the current user with the session
     */
    suspend fun getUser(): UserInfo

    class Config

    companion object : SupabasePlugin<Config, Auth> {

        override val key = "auth"

        override fun create(supabaseClient: SupabaseClient, config: Config.() -> Unit): Auth = AuthImpl(supabaseClient, Config().apply(config))

    }

}

val SupabaseClient.auth: Auth
    get() = plugins.getOrElse("auth") {
        throw IllegalStateException("Auth plugin not installed")
    } as? Auth ?: throw IllegalStateException("Auth plugin not installed")
package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.providers.AuthProvider
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.serialization.Serializable

class SSO<Config: SSO.Config> private constructor(val config: Config): AuthProvider<Config, Unit> {

    sealed class Config {
        var captchaToken: String? = null
        data class Domain(val domain: String) : Config()
        data class Provider(val providerId: String) : Config()
    }

    @Serializable
    data class Result(
        val url: String
    )

    companion object {

        fun withDomain(domain: String, config: (Config.() -> Unit)? = null): SSO<Config> = SSO(Config.Domain(domain).apply {
            config?.invoke(this)
        })

        fun withProvider(providerId: String, config: (Config.() -> Unit)? = null): SSO<Config> = SSO(Config.Provider(providerId).apply {
            config?.invoke(this)
        })

    }

    override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (Config.() -> Unit)?
    ) = signUp(supabaseClient, onSuccess, redirectUrl, config)

    override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (Config.() -> Unit)?
    ) = loginWithSSO(
        supabaseClient,
        onSuccess,
        redirectUrl,
        config
    )



}

internal expect suspend fun <Config : SSO.Config> SSO<Config>.loginWithSSO(
    supabaseClient: SupabaseClient,
    onSuccess: suspend (UserSession) -> Unit,
    redirectUrl: String?,
    config: (Config.() -> Unit)?
)
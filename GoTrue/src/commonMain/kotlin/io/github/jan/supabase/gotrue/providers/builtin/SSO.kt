package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotiations.SupabaseExperimental
import io.github.jan.supabase.gotrue.providers.AuthProvider
import io.github.jan.supabase.gotrue.providers.builtin.SSO.Companion.withDomain
import io.github.jan.supabase.gotrue.providers.builtin.SSO.Companion.withProvider
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.serialization.Serializable

/**
 * Single Sign On (SSO) auth provider for supabase.
 *
 * Check the [docs](https://supabase.com/docs/guides/auth/sso/auth-sso-saml) for more information.
 *
 * Create a new instance with [withDomain] or [withProvider].
 *
 * @param config The config for the SSO provider
 */
class SSO<Config: SSO.Config> private constructor(val config: Config): AuthProvider<Config, Unit> {

    /**
     * The SSO config
     */
    sealed class Config {

        /**
         * Optional captcha token
         */
        var captchaToken: String? = null

        /**
         * Config for a SSO provider with a domain
         * @param domain The domain of the SSO provider
         */
        data class Domain(val domain: String) : Config()

        /**
         * Config for a SSO provider with a provider id
         * @param providerId The provider id of the SSO provider
         */
        data class Provider(val providerId: String) : Config()
    }

    /**
     * The result of a SSO login
     * @param url The url to redirect to
     */
    @Serializable
    data class Result(
        val url: String
    )

    companion object {

        /**
         * Create a new SSO instance with a domain
         */
        @SupabaseExperimental
        fun withDomain(domain: String, config: (Config.() -> Unit)? = null): SSO<Config> = SSO(Config.Domain(domain).apply {
            config?.invoke(this)
        })

        /**
         * Create a new SSO instance with a provider id
         */
        @SupabaseExperimental
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
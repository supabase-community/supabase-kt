package io.github.jan.supabase.gotrue.providers.builtin

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.GoTrueImpl
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.AuthProvider
import io.github.jan.supabase.gotrue.user.UserSession
import io.ktor.client.call.body
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed interface SSO<Config : SSO.Config>: AuthProvider<Config, SSO.Result> {

    sealed class Config {
        var captchaToken: String? = null
    }

    @Serializable
    data class Result(
        val url: String
    )

    object Domain: SSO<Domain.Config> {

        data class Config(
            var domain: String = "",
        ): SSO.Config()

        override fun createConfig(config: (Config.() -> Unit)?): Config = Config().apply { config?.invoke(this) }

    }

    object Provider: SSO<Provider.Config> {

        data class Config(
            var providerId: String = "",
        ): SSO.Config()

        override fun createConfig(config: (Config.() -> Unit)?) = Config().apply { config?.invoke(this) }

    }

    fun createConfig(config: (Config.() -> Unit)?) : Config

    override suspend fun login(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (Config.() -> Unit)?
    ): Unit = error("")

    override suspend fun signUp(
        supabaseClient: SupabaseClient,
        onSuccess: suspend (UserSession) -> Unit,
        redirectUrl: String?,
        config: (Config.() -> Unit)?
    ): Result {
        val createdConfig = createConfig(config)
        val api = (supabaseClient.gotrue as GoTrueImpl).api
        return api.postJson("sso", buildJsonObject {
            redirectUrl?.let { put("redirect_to", it) }
            createdConfig.captchaToken?.let {
                put("gotrue_meta_security", buildJsonObject {
                    put("captcha_token", it)
                })
            }
            when(createdConfig) {
                is Domain.Config -> put("domain", createdConfig.domain)
                is Provider.Config -> put("provider_id", createdConfig.providerId)
            }
        }).body()
    }



}
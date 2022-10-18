package io.github.jan.supabase.gotrue.admin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

sealed interface LinkType<C: LinkType.Config> {

    val type: String

    fun createConfig(config: C.() -> Unit): C

    @Serializable
    open class Config {

        var email: String = ""

    }

    object Signup: LinkType<Signup.Config> {

        override val type = "signup"

        @Serializable
        data class Config(
            var password: String = "",
            var data: JsonObject? = null
        ): LinkType.Config()

        override fun createConfig(config: Config.() -> Unit): Config = Config().apply(config)

    }

    object Invite : LinkType<Config> {

        override val type = "invite"

        override fun createConfig(config: Config.() -> Unit): Config = Config().apply(config)

    }

    object MagicLink : LinkType<Config> {

        override val type: String = "magiclink"

        override fun createConfig(config: Config.() -> Unit): Config = Config().apply(config)

    }

    object RecoveryLink : LinkType<Config> {

        override val type: String = "recovery"

        override fun createConfig(config: Config.() -> Unit): Config = Config().apply(config)

    }

    object EmailChange : LinkType<EmailChange.Config> {

        override val type: String = "email_change_current"

        @Serializable
        data class Config(
            @SerialName("new_email")
            var newEmail: String = ""
        ): LinkType.Config()

        override fun createConfig(config: Config.() -> Unit): Config = Config().apply(config)

    }

}
package io.github.jan.supabase.gotrue.admin

import io.github.jan.supabase.annotiations.SupabaseInternal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Link types used in [AdminApi.generateLinkFor]
 */
sealed interface LinkType<C: LinkType.Config> {

    /**
     * The type of the link
     */
    val type: String

    @SupabaseInternal
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

        @SupabaseInternal
        override fun createConfig(config: Config.() -> Unit): Config = Config().apply(config)

    }

    object Invite : LinkType<Config> {

        override val type = "invite"

        @SupabaseInternal
        override fun createConfig(config: Config.() -> Unit): Config = Config().apply(config)

    }

    object MagicLink : LinkType<Config> {

        override val type: String = "magiclink"

        @SupabaseInternal
        override fun createConfig(config: Config.() -> Unit): Config = Config().apply(config)

    }

    object RecoveryLink : LinkType<Config> {

        override val type: String = "recovery"

        @SupabaseInternal
        override fun createConfig(config: Config.() -> Unit): Config = Config().apply(config)

    }

    object EmailChangeCurrent : LinkType<EmailChangeCurrent.Config> {

        override val type: String = "email_change_current"

        @Serializable
        data class Config(
            @SerialName("new_email")
            var newEmail: String = ""
        ): LinkType.Config()

        @SupabaseInternal
        override fun createConfig(config: Config.() -> Unit): Config = Config().apply(config)

    }

    object EmailChangeNew : LinkType<EmailChangeCurrent.Config> {

        override val type: String = "email_change_new"

        @SupabaseInternal
        override fun createConfig(config: EmailChangeCurrent.Config.() -> Unit): EmailChangeCurrent.Config = EmailChangeCurrent.Config().apply(config)

    }

}
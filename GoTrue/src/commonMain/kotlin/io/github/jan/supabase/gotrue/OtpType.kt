package io.github.jan.supabase.gotrue

sealed interface OtpType {

    val type: String

    enum class Email(override val type: String): OtpType {
        MAGIC_LINK("magiclink"),
        SIGNUP("signup"),
        INVITE("invite"),
        RECOVERY("recovery"),
        EMAIL_CHANGE("email_change")
    }

    enum class Phone(override val type: String): OtpType {
        SMS("sms"),
        PHONE_CHANGE("phone_change")
    }

}
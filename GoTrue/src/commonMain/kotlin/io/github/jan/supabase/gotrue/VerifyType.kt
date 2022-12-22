package io.github.jan.supabase.gotrue

object VerifyType {

    enum class Email(val value: String) {
        MAGIC_LINK("magiclink"),
        SIGNUP("signup"),
        INVITE("invite"),
        RECOVERY("recovery"),
        EMAIL_CHANGE("email_change")
    }

    enum class Phone(val value: String) {
        SMS("sms"),
        PHONE_CHANGE("phone_change")
    }


}
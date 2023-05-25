package io.github.jan.supabase.gotrue

/**
 * OTPs (One Time Passwords) are used to authenticate users via email or phone.
 *
 * An OTP type can be either [Email] or [Phone]
 */
sealed interface OtpType {

    /**
     * The type of the OTP
     */
    val type: String

    /**
     * Email OTP types
     */
    enum class Email(override val type: String): OtpType {
        MAGIC_LINK("magiclink"),
        SIGNUP("signup"),
        INVITE("invite"),
        RECOVERY("recovery"),
        EMAIL_CHANGE("email_change")
    }

    /**
     * Phone OTP types
     */
    enum class Phone(override val type: String): OtpType {
        SMS("sms"),
        PHONE_CHANGE("phone_change")
    }

}
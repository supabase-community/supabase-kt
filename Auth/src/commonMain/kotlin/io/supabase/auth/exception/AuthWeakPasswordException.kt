package io.supabase.auth.exception

/**
 * Exception thrown on sign-up if the password is too weak
 * @param description The description of the exception.
 * @param reasons The reasons why the password is weak.
 */
class AuthWeakPasswordException(
    description: String,
    statusCode: Int,
    val reasons: List<String>
) : AuthRestException(
    CODE,
    description,
    statusCode
) {

    internal companion object {
        const val CODE = "weak_password"
    }

}

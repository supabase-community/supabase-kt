package io.github.jan.supabase.gotrue.exception

/**
 * Exception thrown when a session is not found.
 * @param description The description of the exception.
 * @param reasons The reasons why the password is weak.
 */
class AuthWeakPasswordException(
    description: String,
    val reasons: List<String>
) : AuthRestException(
    CODE,
    description,
) {

    companion object {
        const val CODE = "weak_password"
    }

}
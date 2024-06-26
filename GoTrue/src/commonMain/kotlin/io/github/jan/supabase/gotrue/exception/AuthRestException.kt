package io.github.jan.supabase.gotrue.exception

import io.github.jan.supabase.exceptions.RestException

/**
 * Base class for rest exceptions thrown by the Auth API.
 * @property errorCode The error code of the rest exception. This should be a known [AuthErrorCode]. If it is not, use [error] instead.
 * @param message The message of the rest exception.
 */
open class AuthRestException(errorCode: String, message: String, statusCode: Int): RestException(
    error = errorCode,
    description = "Auth API error: $errorCode",
    message = message,
    statusCode = statusCode
) {

    /**
     * The error code of the rest exception. If [errorCode] is not a known [AuthErrorCode], this will be null. Then, use [error] instead to get the raw unknown error code.
     */
    val errorCode: AuthErrorCode? = AuthErrorCode.fromValue(errorCode)

}
package io.github.jan.supabase.auth.exception

import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.statement.HttpResponse

/**
 * Base class for rest exceptions thrown by the Auth API.
 * @property errorCode The error code of the rest exception. This should be a known [AuthErrorCode]. If it is not, use [error] instead.
 * @param message The message of the rest exception.
 */
open class AuthRestException(errorCode: String, description: String, response: HttpResponse): RestException(
    error = errorCode,
    description = "$description: $errorCode",
    response = response
) {

    /**
     * The error code of the rest exception. If [errorCode] is not a known [AuthErrorCode], this will be null. Then, use [error] instead to get the raw unknown error code.
     */
    val errorCode: AuthErrorCode? = AuthErrorCode.fromValue(errorCode)

}
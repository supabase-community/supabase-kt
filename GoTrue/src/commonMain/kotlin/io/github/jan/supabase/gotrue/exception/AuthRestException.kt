package io.github.jan.supabase.gotrue.exception

import io.github.jan.supabase.exceptions.RestException

/**
 * Base class for rest exceptions thrown by the Auth API.
 */
open class AuthRestException(errorCode: String, message: String): RestException(
    error = errorCode,
    description = null,
    message = message
)
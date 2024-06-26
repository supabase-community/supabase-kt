package io.github.jan.supabase.exceptions

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request

/**
 * Base class for all response-related exceptions
 *
 * Plugins may extend this class to provide more specific exceptions
 * @param error The error returned by Supabase
 * @param description The error description returned by Supabase
 * @param statusCode The HTTP status code of the rest exception.
 * @see UnauthorizedRestException
 * @see BadRequestRestException
 * @see NotFoundRestException
 * @see UnknownRestException
 */
open class RestException(val error: String, val description: String?, val statusCode: Int, message: String): Exception(message) {

    constructor(error: String, response: HttpResponse, message: String? = null): this(error, message, response.status.value, """
        $error${message?.let { " ($it)" } ?: ""}
        URL: ${response.request.url}
        Headers: ${response.request.headers.entries()}
        Http Method: ${response.request.method.value}
    """.trimIndent())

}

/**
 * Thrown when supabase-kt receives a response indicating an authentication error
 */
class UnauthorizedRestException(error: String, response: HttpResponse, message: String? = null): RestException(error, response, message)

/**
 * Thrown when supabase-kt receives a response indicating that the request was invalid due to missing or wrong fields
 */
class BadRequestRestException(error: String, response: HttpResponse, message: String? = null): RestException(error, response, message)

/**
 * Thrown when supabase-kt receives a response indicating that the wanted resource was not found
 */
class NotFoundRestException(error: String, response: HttpResponse, message: String? = null): RestException(error, response, message)

/**
 * Thrown for all other response codes
 */
class UnknownRestException(error: String, response: HttpResponse, message: String? = null): RestException(error, response, message)

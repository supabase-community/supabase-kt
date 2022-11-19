package io.github.jan.supabase.exceptions

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request

/**
 * Base class for all response-related exceptions
 */
sealed class RestException(message: String): Exception(message) {

    constructor(error: String, response: HttpResponse, message: String? = null): this("""
        $error ${message?.let { ":($it)" } ?: ""}
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

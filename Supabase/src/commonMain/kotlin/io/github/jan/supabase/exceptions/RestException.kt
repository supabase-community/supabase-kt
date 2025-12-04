package io.github.jan.supabase.exceptions

import io.github.jan.supabase.maskString
import io.github.jan.supabase.maskUrl
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.Headers
import io.ktor.util.toMap

/**
 * Base class for all response-related exceptions
 *
 * Plugins may extend this class to provide more specific exceptions
 * @param error The error returned by Supabase
 * @param description The error description returned by Supabase
 * @param response The response that caused the exception
 * @see UnauthorizedRestException
 * @see BadRequestRestException
 * @see NotFoundRestException
 * @see UnknownRestException
 */
open class RestException(val error: String, val description: String?, val response: HttpResponse): Exception("""
        $error${description?.let { "\n$it" } ?: ""}
        URL: ${maskUrl(response.request.url)}
        Headers: ${maskHeaders(response.request.headers)}
        Http Method: ${response.request.method.value}
""".trimIndent()) {

    /**
     * The status code of the response
     */
    val statusCode = response.status.value

}

private val SENSITIVE_HEADERS = listOf("apikey", "Authorization")

private fun maskHeaders(headers: Headers): String = headers.toMap().mapValues { (key, value) ->
    if(key in SENSITIVE_HEADERS) {
        value.firstOrNull()?.let {
            listOf(if(key == "Authorization") "Bearer ${maskString(it.drop(7), showLength = true)}" else maskString(it, showLength = true))
        }
    } else value
}.toString()

/**
 * Thrown when supabase-kt receives a response indicating an authentication error
 */
class UnauthorizedRestException(error: String, response: HttpResponse, message: String? = null): RestException(error,  message, response)

/**
 * Thrown when supabase-kt receives a response indicating that the request was invalid due to missing or wrong fields
 */
class BadRequestRestException(error: String, response: HttpResponse, message: String? = null): RestException(error, message, response)

/**
 * Thrown when supabase-kt receives a response indicating that the wanted resource was not found
 */
class NotFoundRestException(error: String, response: HttpResponse, message: String? = null): RestException(error, message, response)

/**
 * Thrown for all other response codes
 */
class UnknownRestException(error: String, response: HttpResponse, message: String? = null): RestException(error, message, response)

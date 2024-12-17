package io.github.jan.supabase.postgrest.exception

import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.statement.HttpResponse

/**
 * Exception thrown when a Postgrest request fails
 * @param message The error message
 * @param hint A hint to the error
 * @param details Additional details about the error
 * @param code The error code
 * @param response The response that caused the exception
 */
class PostgrestRestException(
    message: String,
    val hint: String?,
    val details: String?,
    val code: String?,
    response: HttpResponse
): RestException(message, hint ?: details, response)
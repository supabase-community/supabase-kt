package io.supabase.exceptions

import io.ktor.client.request.HttpRequestBuilder

/**
 * An exception that is thrown when a request fails due to network issues
 */
class HttpRequestException(message: String, request: HttpRequestBuilder): Exception("HTTP request to ${request.url.buildString()} (${request.method.value}) failed with message: $message")
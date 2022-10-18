package io.github.jan.supabase.gotrue

import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText

suspend fun HttpResponse.checkErrors(error: String = "Error while performing request"): HttpResponse {
    if(status.value !in 200..299) {
        throw RestException(status.value, error, bodyAsText(), headers.entries().flatMap { (key, value) -> listOf(key) + value })
    }
    return this
}
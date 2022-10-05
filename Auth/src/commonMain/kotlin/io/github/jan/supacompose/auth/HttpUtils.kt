package io.github.jan.supacompose.auth

import io.github.jan.supacompose.exceptions.RestException
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

suspend fun HttpResponse.checkErrors(error: String = "Error while performing request"): HttpResponse {
    if(status.value !in 200..299) {
        throw RestException(status.value, error, bodyAsText(), headers.entries().flatMap { (key, value) -> listOf(key) + value })
    }
    return this
}
package io.github.jan.supacompose.auth

import io.github.jan.supacompose.exceptions.RestException
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.JsonObject

suspend fun HttpResponse.checkErrors() {
    if(status.value !in 200..299) {
        throw RestException(status.value, "Error while performing request", body<JsonObject>().toString())
    }
}
package io.github.jan.supabase.testing

import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

suspend fun OutgoingContent.toJsonElement(): JsonElement {
    return Json.decodeFromString(toByteArray().decodeToString())
}

fun MockRequestHandleScope.respondJson(json: String, code: HttpStatusCode = HttpStatusCode.OK): HttpResponseData {
    return respond(json, headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()), status = code)
}

inline fun <reified T> MockRequestHandleScope.respondJson(json: T, code: HttpStatusCode = HttpStatusCode.OK): HttpResponseData = respondJson(Json.encodeToString(json), code)
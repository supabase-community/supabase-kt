package io.github.jan.supabase.testing

import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.content.OutgoingContent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

suspend fun OutgoingContent.toJsonElement(): JsonElement {
    return Json.decodeFromString(toByteArray().decodeToString())
}
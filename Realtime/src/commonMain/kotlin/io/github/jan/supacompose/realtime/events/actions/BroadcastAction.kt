package io.github.jan.supacompose.realtime.events.actions

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

class BroadcastAction(val event: String, val data: JsonObject): ChannelAction

/**
 * Decodes [BroadcastAction.data] as [T] and returns it or returns null when it cannot be decoded as [T]
 */
inline fun <reified T> BroadcastAction.decodeDataOrNull(json: Json = Json): T? {
    return try {
        json.decodeFromJsonElement<T>(data)
    } catch (e: Exception) {
        null
    }
}

/**
 * Decodes [HasRecord.record] as [T] and returns it
 */
inline fun <reified T> BroadcastAction.decodeData(json: Json = Json) = json.decodeFromJsonElement<T>(data)
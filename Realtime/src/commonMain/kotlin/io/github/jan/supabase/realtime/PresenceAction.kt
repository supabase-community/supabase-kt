package io.github.jan.supabase.realtime

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * @property presenceRef The presence reference of the object
 * @property state The state is the object the other client is tracking. Can be done via the [RealtimeChannel.track] method
 */
@Serializable(with = Presence.Companion::class)
data class Presence(
    val presenceRef: String,
    val state: JsonObject
) {

    companion object : KSerializer<Presence> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("io.github.jan.supabase.realtime.Presence") {
            element("phx_ref", String.serializer().descriptor)
            element("state", JsonObject.serializer().descriptor)
        }

        override fun deserialize(decoder: Decoder): Presence {
            decoder as JsonDecoder
            val json = decoder.decodeJsonElement().jsonObject
            val meta = json["metas"]?.jsonArray?.get(0)?.jsonObject ?: error("A presence should at least have a phx_ref. Full json: $json")
            val presenceRef = meta["phx_ref"]?.jsonPrimitive?.contentOrNull ?: error("A presence should at least have a phx_ref. Full json: $json")
            return Presence(presenceRef, JsonObject(meta.toMutableMap().apply {
                remove("phx_ref")
            }))
        }

        override fun serialize(encoder: Encoder, value: Presence) {
            encoder as JsonEncoder
            encoder.encodeJsonElement(buildJsonObject {
                put("phx_ref", value.presenceRef)
                put("state", value.state)
            })
        }

    }

    /**
     * Decodes [state] as [T]
     *
     * **Note: You also receive your own presence, but without your state so be aware of exceptions or use [stateAsOrNull] instead**
     */
    inline fun <reified T> stateAs(json: Json): T {
        return json.decodeFromJsonElement(state)
    }

    /**
     * Decodes [state] as [T] or null if there is an exception while decoding
     */
    inline fun <reified T> stateAsOrNull(json: Json): T? {
        return try {
            stateAs(json)
        } catch (e: Exception) {
            null
        }
    }

}

/**
 * Represents a presence action
 */
sealed interface PresenceAction {

    /**
     * Represents a map of [Presence] objects indexed by their key.
     * Your own key can be customized when creating the channel within the presence config
     */
    val joins: Map<String, Presence>

    /**
     * Represents a map of [Presence] objects indexed by their key.
     * Your own key can be customized when creating the channel within the presence config
     */
    val leaves: Map<String, Presence>

}

internal class PresenceActionImpl(
    override val joins: Map<String, Presence>,
    override val leaves: Map<String, Presence>
) : PresenceAction

/**
 * Decodes all [PresenceAction.joins] values as [T]
 * @param json The [Json] instance to use for decoding
 * @param ignoreOtherTypes Whether to ignore presences which cannot be decoded as [T]
 */
inline fun <reified T> PresenceAction.decodeJoinsAs(json: Json = Json, ignoreOtherTypes: Boolean = true): List<T> = joins.values.mapNotNull {
    if (ignoreOtherTypes) {
        it.stateAsOrNull<T>(json)
    } else {
        it.stateAs<T>(json)
    }
}

/**
 * Decodes all [PresenceAction.leaves] values as [T]
 * @param json The [Json] instance to use for decoding
 * @param ignoreOtherTypes Whether to ignore presences which cannot be decoded as [T]
 */
inline fun <reified T> PresenceAction.decodeLeavesAs(json: Json = Json, ignoreOtherTypes: Boolean = true): List<T> = leaves.values.mapNotNull {
    if (ignoreOtherTypes) {
        it.stateAsOrNull<T>(json)
    } else {
        it.stateAs<T>(json)
    }
}
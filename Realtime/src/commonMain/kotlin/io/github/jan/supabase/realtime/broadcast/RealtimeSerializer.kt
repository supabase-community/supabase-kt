package io.github.jan.supabase.realtime.broadcast

import io.github.jan.supabase.realtime.RealtimeBroadcast
import io.github.jan.supabase.realtime.RealtimeMessage
import io.ktor.utils.io.core.buildPacket
import kotlinx.io.Source
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal object RealtimeSerializer {

    fun decodeBinaryPayload(data: ByteArray): RealtimeBroadcast<*> {
        if(data.size < 5) error("Binary frame too short: ${data.size} bytes")
        val kind = data[0]
        if(kind != RealtimeSerializer.BinaryKind.USER_BROADCAST.value) error("Unexpected frame kind: $kind, expected ${RealtimeSerializer.BinaryKind.USER_BROADCAST.value}")
        val topicLen = data[1].toInt() and 0xFF
        val eventLen = data[2].toInt() and 0xFF
        val metaLen = data[3].toInt() and 0xFF
        val encoding = RealtimeSerializer.PayloadEncoding.from(data[4]) ?: error("Unknown payload encoding: ${data[4]}")
        val headerSize = 5
        val expectedMinSize = headerSize+topicLen+eventLen+metaLen
        if(data.size < expectedMinSize) error("Payload too short for declared field lengths")
        var offset = headerSize
        val topic = data.slice(offset..<offset+topicLen).toByteArray().decodeToString()
        offset += topicLen
        val event = data.slice(offset..<offset+eventLen).toByteArray().decodeToString()
        offset += eventLen
        offset += metaLen
        val payload = data.slice(offset..<data.size).toByteArray()
        return when(encoding) {
            PayloadEncoding.BINARY -> RealtimeBroadcast.Binary(topic, event, payload)
            PayloadEncoding.JSON -> RealtimeBroadcast.Json(topic, event, payload.decodeToString())
        }
    }

    fun encodeBroadcast(
        joinRef: String?,
        ref: String?,
        broadcast: RealtimeBroadcast<*>,
        encoding: PayloadEncoding
    ): Source {
        val joinRefBytes = (joinRef ?: "").encodeToByteArray()
        val refBytes = (ref ?: "").encodeToByteArray()
        val topicBytes = broadcast.topic.encodeToByteArray()
        val eventBytes = broadcast.event.encodeToByteArray()
        val metaBytes = ByteArray(0)
        val payload = when(broadcast) {
            is RealtimeBroadcast.Binary -> broadcast.payload
            is RealtimeBroadcast.Json -> broadcast.payload.encodeToByteArray()
        }

        if(joinRefBytes.size > 255 || refBytes.size > 255 || topicBytes.size > 255 || eventBytes.size > 255 || metaBytes.size > 255)
            error("Binary frame header fields must not exceed 255 bytes each")

        return buildPacket {
            writeByte(BinaryKind.USER_BROADCAST_PUSH.value)
            writeByte(joinRefBytes.size.toByte())
            writeByte(refBytes.size.toByte())
            writeByte(topicBytes.size.toByte())
            writeByte(eventBytes.size.toByte())
            writeByte(metaBytes.size.toByte())
            writeByte(encoding.value)
            write(joinRefBytes)
            write(refBytes)
            write(topicBytes)
            write(eventBytes)
            write(metaBytes)
            write(payload)
        }
    }

    fun decodeV2Text(text: String): RealtimeMessage {
        val array = Json.decodeFromString<JsonArray>(text)
        if(array.size < 5) error("Expected array with 5 elements, got ${array.size}")
      //  val joinRef = array[0].jsonPrimitive.contentOrNull
        val ref = array[1].jsonPrimitive.contentOrNull
        val topic = array[2].jsonPrimitive.contentOrNull ?: error("Expected string for topic at index 2")
        val event = array[3].jsonPrimitive.contentOrNull ?: error("Expected string for event at index 3")
        val payload = array.getOrNull(4)?.jsonObject ?: error("Expected object for payload at index 4")
        return RealtimeMessage(topic, event, payload, ref)
    }

    enum class BinaryKind(val value: Byte) {
        USER_BROADCAST_PUSH(3),
        USER_BROADCAST(4)
    }

    enum class PayloadEncoding(val value: Byte) {
        BINARY(0),
        JSON(1);

        companion object {
            fun from(value: Byte) = entries.find { it.value == value }
        }
    }

}
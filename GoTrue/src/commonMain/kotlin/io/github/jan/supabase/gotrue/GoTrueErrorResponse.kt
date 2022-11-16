package io.github.jan.supabase.gotrue

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = GoTrueErrorResponse.Companion::class)
internal data class GoTrueErrorResponse(
    val error: String
) {

    companion object : KSerializer<GoTrueErrorResponse> {

        override val descriptor = buildClassSerialDescriptor("GoTrueErrorResponse") {
            element("error", String.serializer().descriptor)
        }

        override fun deserialize(decoder: Decoder): GoTrueErrorResponse {
            decoder as JsonDecoder
            val json = decoder.decodeJsonElement()
            val error = json.jsonObject["error"]?.jsonPrimitive?.content ?: json.jsonObject["msg"]?.jsonPrimitive?.content ?: json.toString()
            return GoTrueErrorResponse(error)
        }

        override fun serialize(encoder: Encoder, value: GoTrueErrorResponse) {
            throw UnsupportedOperationException()
        }

    }

}
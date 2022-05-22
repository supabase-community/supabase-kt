package io.github.jan.supacompose.auth.serializers

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeSpanFormat
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.parse
import com.soywiz.klock.parseUtc
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = DateTimeTz::class)
object DateTimeSerializer: KSerializer<DateTimeTz> {

    override val descriptor = PrimitiveSerialDescriptor("DateTimeTz", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder) = DateFormat("yyyy-MM-dd'T'HH:mm:ss.XXXZ").parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: DateTimeTz) {
        TODO("Not yet implemented")
    }

}
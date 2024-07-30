package io.github.jan.supabase.serializer

import io.github.jan.supabase.SupabaseSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KType

/**
 * A [SupabaseSerializer] that uses kotlinx.serialization
 */
class KotlinXSerializer(private val json: Json = Json) : SupabaseSerializer {

    override fun <T : Any> encode(type: KType, value: T): String = json.encodeToString(serializer(type), value)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> decode(type: KType, value: String): T =
        json.decodeFromString(serializer(type), value) as T

}
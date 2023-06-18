package io.github.jan.supabase

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * An interface for serializing and deserializing objects.
 * Note: Internally, KotlinX Serialization will always be used.
 */
interface SupabaseSerializer {

    /**
     * Encodes the given [value] to a string
     */
    fun <T : Any> encode(type: KClass<T>, value: T): String

    /**
     * Decodes the given [value] to an object of type [T]
     */
    fun <T : Any> decode(type: KClass<T>, value: String): T

}

/**
 * A [SupabaseSerializer] that uses kotlinx.serialization
 */
class KotlinXSupabaseSerializer(private val json: Json = Json) : SupabaseSerializer {

    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> encode(type: KClass<T>, value: T): String = json.encodeToString(type.serializer(), value)

    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> decode(type: KClass<T>, value: String): T = json.decodeFromString(type.serializer(), value)

}
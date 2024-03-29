package io.github.jan.supabase

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * An interface for serializing and deserializing objects.
 * Note: Internally, KotlinX Serialization will always be used.
 */
interface SupabaseSerializer {

    /**
     * Encodes the given [value] to a string
     */
    fun <T : Any> encode(type: KType, value: T): String

    /**
     * Decodes the given [value] to an object of type [T]
     */
    fun <T : Any> decode(type: KType, value: String): T

}

/**
 * Encodes the given [value] to a string
 */
inline fun <reified T : Any> SupabaseSerializer.encode(value: T): String = encode(typeOf<T>(), value)

/**
 * Encodes the given [value] to a [JsonElement]
 */
inline fun <reified T : Any> SupabaseSerializer.encodeToJsonElement(value: T): JsonElement = Json.decodeFromString(encode(value))

/**
 * Decodes the given [value] to an object of type [T]
 */
inline fun <reified T : Any> SupabaseSerializer.decode(value: String): T = decode(typeOf<T>(), value)
package io.github.jan.supabase.serializer

import io.github.jan.supabase.annotations.SupabaseInternal
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@SupabaseInternal
inline fun <reified T> JsonArray.mapValue(key: String) =
    map { it.jsonObject[key]?.jsonPrimitive?.contentOrNull ?: throw SerializationException("Key $key missing in object.\n Body: $this") }

package io.github.jan.supabase.storage

import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.encodeToJsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject

class FileOptionBuilder(
    @PublishedApi internal val serializer: SupabaseSerializer,
    var userMetadata: JsonObject? = null,
) {

    inline fun <reified T : Any> userMetadata(data: T) {
        userMetadata = serializer.encodeToJsonElement(data).jsonObject
    }

    inline fun userMetadata(builder: JsonObjectBuilder.() -> Unit) {
        userMetadata = buildJsonObject(builder)
    }

}

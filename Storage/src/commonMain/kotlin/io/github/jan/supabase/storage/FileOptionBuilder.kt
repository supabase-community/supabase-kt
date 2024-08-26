package io.github.jan.supabase.storage

import io.github.jan.supabase.SupabaseSerializer
import io.github.jan.supabase.encodeToJsonElement
import io.ktor.http.ContentType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Builder for uploading files with additional options
 * @param serializer The serializer to use for encoding the metadata
 * @param userMetadata The user metadata to upload with the file
 * @param upsert Whether to update the file if it already exists
 * @param contentType The content type of the file. If null, the content type will be inferred from the file extension
 */
class FileOptionBuilder(
    @PublishedApi internal val serializer: SupabaseSerializer,
    var userMetadata: JsonObject? = null,
    var upsert: Boolean = false,
    var contentType: ContentType? = null,
) {

    /**
     * Sets the user metadata to upload with the file
     * @param data The data to upload. Must be serializable by the [serializer]
     */
    inline fun <reified T : Any> userMetadata(data: T) {
        userMetadata = serializer.encodeToJsonElement(data).jsonObject
    }

    /**
     * Sets the user metadata to upload with the file
     * @param builder The builder for the metadata
     */
    inline fun userMetadata(builder: JsonObjectBuilder.() -> Unit) {
        userMetadata = buildJsonObject(builder)
    }

}

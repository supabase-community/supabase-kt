package io.supabase.storage

import io.supabase.SupabaseSerializer
import io.supabase.network.HttpRequestOverride
import io.ktor.http.ContentType

/**
 * Builder for uploading files with additional options
 * @param serializer The serializer to use for encoding the metadata
 * @param upsert Whether to update the file if it already exists
 * @param contentType The content type of the file. If null, the content type will be inferred from the file extension
 */
class UploadOptionBuilder(
    @PublishedApi internal val serializer: SupabaseSerializer,
    var upsert: Boolean = false,
    var contentType: ContentType? = null,
    internal val httpRequestOverrides: MutableList<HttpRequestOverride> = mutableListOf()
) {

    /**
     * Overrides the HTTP request
     */
    fun httpOverride(override: HttpRequestOverride) {
        httpRequestOverrides.add(override)
    }

}

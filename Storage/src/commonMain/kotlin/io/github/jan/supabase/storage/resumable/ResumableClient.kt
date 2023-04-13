package io.github.jan.supabase.storage.resumable

import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.storage
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class ResumableClient(private val storageApi: BucketApi, private val cache: ResumableCache) {

    private val httpClient = storageApi.supabaseClient.httpClient.httpClient
    private val url = storageApi.supabaseClient.storage.resolveUrl("upload/resumable")

    suspend fun startOrResumeUpload(channel: ByteReadChannel, size: Long, path: String): ResumableUpload {
        val cachedUrl = cache.get("${size}-$path")
        val chunkSize = storageApi.supabaseClient.storage.config.defaultChunkSize
        if(cachedUrl != null) {
            val response = httpClient.request(cachedUrl) {
                header("Upload-Metadata", encodeMetadata(createMetadata(path)))
                method = HttpMethod.Head
                header("Tus-Resumable", "1.0.0")
            }
            val offset = response.headers["Upload-Offset"]?.toLongOrNull() ?: 0
            if(offset < size) {
                return ResumableUpload(path, channel.apply { discard(offset) }, size, offset, chunkSize, cachedUrl, httpClient, storageApi) {
                    cache.remove("${size}-$path")
                }
            } else error("File already uploaded")
        }
        val response = httpClient.post(url) {
            header("Upload-Metadata", encodeMetadata(createMetadata(path)))
            bearerAuth(accessTokenOrApiKey())
            header("Upload-Length", size)
            header("Tus-Resumable", "1.0.0")
        }
        val uploadUrl = response.headers["Location"] ?: error("No upload url found")
        cache.set("${size}-$path", uploadUrl)
        return ResumableUpload(path, channel, size, 0, chunkSize, uploadUrl, httpClient, storageApi) {
            cache.remove("${size}-$path")
        }
    }

    suspend fun startOrResumeUpload(data: ByteArray, path: String) = startOrResumeUpload(ByteReadChannel(data), data.size.toLong(), path)

    private fun accessTokenOrApiKey() = storageApi.supabaseClient.pluginManager.getPluginOrNull(GoTrue)?.currentAccessTokenOrNull() ?: storageApi.supabaseClient.supabaseKey

    private fun createMetadata(path: String): Map<String, String> = buildMap {
        put("bucketName", storageApi.bucketId)
        put("objectName", path)
        put("contentType", ContentType.defaultForFilePath(path).toString())
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun encodeMetadata(metadata: Map<String, String>): String {
        return metadata.entries.joinToString(",") { (key, value) ->
            key + " " + Base64.encode(value.toByteArray())
        }
    }

}

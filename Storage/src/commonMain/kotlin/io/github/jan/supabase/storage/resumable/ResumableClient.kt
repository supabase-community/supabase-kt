@file:OptIn(ExperimentalEncodingApi::class, ExperimentalEncodingApi::class)

package io.github.jan.supabase.storage.resumable

import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.resumable.ResumableClient.Companion.TUS_VERSION
import io.github.jan.supabase.storage.storage
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.defaultForFilePath
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.toByteArray
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Represents a resumable client. Can be used to create or continue resumable uploads.
 */
sealed interface ResumableClient {

    /**
     * Creates a new resumable upload or continues an existing one.
     * If there is an url in the cache for the given [Fingerprint], the upload will be continued.
     * @param channel The data to upload as a [ByteReadChannel]
     * @param size The size of the data to upload
     * @param path The path to upload the data to
     * @param upsert Whether to overwrite existing files
     */
    suspend fun createOrContinueUpload(channel: ByteReadChannel, size: Long, path: String, upsert: Boolean = false): ResumableUpload

    /**
     * Creates a new resumable upload or continues an existing one.
     * If there is an url in the cache for the given [Fingerprint], the upload will be continued.
     * @param data The data to upload as a [ByteArray]
     * @param path The path to upload the data to
     * @param upsert Whether to overwrite existing files
     */
    suspend fun createOrContinueUpload(data: ByteArray, path: String, upsert: Boolean = false) = createOrContinueUpload(ByteReadChannel(data), data.size.toLong(), path)

    companion object {
        const val TUS_VERSION = "1.0.0"
    }

}

internal class ResumableClientImpl(private val storageApi: BucketApi, private val cache: ResumableCache): ResumableClient {

    private val httpClient = storageApi.supabaseClient.httpClient.httpClient
    private val url = storageApi.supabaseClient.storage.resolveUrl("upload/resumable")

    override suspend fun createOrContinueUpload(channel: ByteReadChannel, size: Long, path: String, upsert: Boolean): ResumableUpload {
        val cachedUrl = cache.get(Fingerprint(storageApi.bucketId, path, size))
        val chunkSize = storageApi.supabaseClient.storage.config.defaultChunkSize
        if(cachedUrl != null) {
            val response = httpClient.request(cachedUrl) {
                header("Upload-Metadata", encodeMetadata(createMetadata(path)))
                method = HttpMethod.Head
                header("Tus-Resumable", TUS_VERSION)
            }
            val offset = response.headers["Upload-Offset"]?.toLongOrNull() ?: 0
            if(offset < size) {
                return ResumableUploadImpl(path, channel.apply { discard(offset) }, size, offset, chunkSize, cachedUrl, httpClient, storageApi) {
                    cache.remove(Fingerprint(storageApi.bucketId, path, size))
                }
            } else error("File already uploaded")
        }
        val response = httpClient.post(url) {
            header("Upload-Metadata", encodeMetadata(createMetadata(path)))
            bearerAuth(accessTokenOrApiKey())
            header("Upload-Length", size)
            header("Tus-Resumable", TUS_VERSION)
            header("x-upsert", upsert.toString())
        }
        when(response.status) {
            HttpStatusCode.Conflict -> error("Specified path already exists. Consider setting upsert to true")
            else -> {
                if(!response.status.isSuccess()) error("Upload failed with status ${response.status}. Message: ${response.bodyAsText()}")
            }
        }
        val uploadUrl = response.headers["Location"] ?: error("No upload url found")
        cache.set(Fingerprint(storageApi.bucketId, path, size), uploadUrl)
        return ResumableUploadImpl(path, channel, size, 0, chunkSize, uploadUrl, httpClient, storageApi) {
            cache.remove(Fingerprint(storageApi.bucketId, path, size))
        }
    }

    private fun accessTokenOrApiKey() = storageApi.supabaseClient.pluginManager.getPluginOrNull(GoTrue)?.currentAccessTokenOrNull() ?: storageApi.supabaseClient.supabaseKey

    private fun createMetadata(path: String): Map<String, String> = buildMap {
        put("bucketName", storageApi.bucketId)
        put("objectName", path)
        put("contentType", ContentType.defaultForFilePath(path).toString())
    }

    private fun encodeMetadata(metadata: Map<String, String>): String {
        return metadata.entries.joinToString(",") { (key, value) ->
            key + " " + Base64.encode(value.toByteArray())
        }
    }

}

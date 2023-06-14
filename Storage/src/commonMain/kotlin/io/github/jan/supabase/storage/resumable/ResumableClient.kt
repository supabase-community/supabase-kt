@file:OptIn(ExperimentalEncodingApi::class, ExperimentalEncodingApi::class)

package io.github.jan.supabase.storage.resumable

import co.touchlab.kermit.Logger
import io.github.jan.supabase.annotations.SupabaseInternal
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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Clock
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Duration.Companion.days

/**
 * Represents a resumable client. Can be used to create or continue resumable uploads.
 */
sealed interface ResumableClient {

    /**
     * Creates a new resumable upload or continues an existing one.
     * If there is an url in the cache for the given [Fingerprint], the upload will be continued.
     * @param channel A function that takes the offset of the upload and returns a [ByteReadChannel] that reads the data to upload from the given offset
     * @param size The size of the data to upload
     * @param path The path to upload the data to
     * @param upsert Whether to overwrite existing files
     */
    suspend fun createOrContinueUpload(channel: suspend (offset: Long) -> ByteReadChannel, source: String, size: Long, path: String, upsert: Boolean = false): ResumableUpload

    /**
     * Creates a new resumable upload or continues an existing one.
     * If there is an url in the cache for the given [Fingerprint], the upload will be continued.
     * @param data The data to upload as a [ByteArray]
     * @param path The path to upload the data to
     * @param upsert Whether to overwrite existing files
     */
    suspend fun createOrContinueUpload(data: ByteArray, source: String, path: String, upsert: Boolean = false) = createOrContinueUpload({ ByteReadChannel(data).apply { discard(it) } }, source, data.size.toLong(), path)

    /**
     * Reads pending uploads from the cache and creates a new [ResumableUpload] for each of them. This done in parallel, so you can start the downloads independently.
     * @param channelProducer A function that takes the source of the upload (e.g. file path) plus the channel offset (you can use [ByteReadChannel.discard] for that) and returns a [ByteReadChannel] for the data to upload
     */
    suspend fun continuePreviousUploads(channelProducer: suspend (source: String, offset: Long) -> ByteReadChannel): List<Deferred<ResumableUpload>>

    companion object {

        /**
         * The TUS (resumable upload protocol) version to use
         */
        const val TUS_VERSION = "1.0.0"
    }

}

internal class ResumableClientImpl(private val storageApi: BucketApi, private val cache: ResumableCache): ResumableClient {

    @OptIn(SupabaseInternal::class)
    private val httpClient = storageApi.supabaseClient.httpClient.httpClient
    private val url = storageApi.supabaseClient.storage.resolveUrl("upload/resumable")
    private val chunkSize = storageApi.supabaseClient.storage.config.resumable.defaultChunkSize

    override suspend fun continuePreviousUploads(channelProducer: suspend (source: String, offset: Long) -> ByteReadChannel): List<Deferred<ResumableUpload>> {
        val cachedEntries = cache.entries()
        return cachedEntries.map { (fingerprint, cacheEntry) ->
            Logger.d { "Found cached upload for ${cacheEntry.path}" }
            coroutineScope {
                async {
                    resumeUpload({ channelProducer(fingerprint.source, it) }, cacheEntry, fingerprint.source, cacheEntry.path, fingerprint.size)
                }
            }
        }
    }

    override suspend fun createOrContinueUpload(
        channel: suspend (offset: Long) -> ByteReadChannel,
        source: String,
        size: Long,
        path: String,
        upsert: Boolean
    ): ResumableUpload {
        val cachedEntry = cache.get(Fingerprint(source, size))
        if(cachedEntry != null) {
            Logger.d { "Found cached upload for $path" }
            return resumeUpload(channel, cachedEntry, source, path, size)
        }
        return createUpload(channel, source, path, size, upsert)
    }

    private suspend fun createUpload(channel: suspend (Long) -> ByteReadChannel, source: String, path: String, size: Long, upsert: Boolean): ResumableUploadImpl {
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
        val fingerprint = Fingerprint(source, size)
        val cacheEntry = ResumableCacheEntry(uploadUrl, path, storageApi.bucketId, Clock.System.now() + 1.days)
        cache.set(fingerprint, cacheEntry)
        return ResumableUploadImpl(fingerprint, path, cacheEntry, channel, 0, chunkSize, uploadUrl, httpClient, storageApi, { retrieveServerOffset(uploadUrl, path) }) {
            cache.remove(fingerprint)
        }
    }

    private suspend fun resumeUpload(channel: suspend (Long) -> ByteReadChannel, entry: ResumableCacheEntry, source: String, path: String, size: Long): ResumableUploadImpl {
        val fingerprint = Fingerprint(source, size)
        if(Clock.System.now() > entry.expiresAt) {
            Logger.d { "Upload url for $path expired. Creating new one" }
            cache.remove(fingerprint)
            return createUpload(channel, source, path, size, false)
        }
        val offset = retrieveServerOffset(entry.url, path)
        if(offset < size) {
            return ResumableUploadImpl(fingerprint, path, entry, channel, offset, chunkSize, entry.url, httpClient, storageApi, { retrieveServerOffset(entry.url, path)}) {
                cache.remove(fingerprint)
            }
        } else error("File already uploaded")
    }

    private suspend fun retrieveServerOffset(url: String, path: String): Long {
        val response = httpClient.request(url) {
            method = HttpMethod.Head
            bearerAuth(accessTokenOrApiKey())
            header("Tus-Resumable", TUS_VERSION)
        }
        if(!response.status.isSuccess()) error("Failed to retrieve server offset: ${response.status} ${response.bodyAsText()}")
        val offset = response.headers["Upload-Offset"]?.toLongOrNull() ?: error("No upload offset found")
        Logger.d { "Server offset for $path is $offset" }
        return offset
    }

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

package io.github.jan.supabase.storage.resumable

import io.github.aakira.napier.Napier
import io.github.jan.supabase.annotiations.SupabaseInternal
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.resumable.ResumableClient.Companion.TUS_VERSION
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.min
import kotlin.time.ExperimentalTime

/**
 * Represents a resumable upload. Can be paused, resumed or cancelled.
 * The upload urls are automatically cashed, so you can resume the upload after your program crashed or the network reconnected without losing the upload progress.
 * You can customize the caching in [Storage.Config]
 */
sealed interface ResumableUpload {

    /**
     * Pauses this upload after the current chunk has been uploaded. Can be resumed using [startOrResumeUploading].
     * If the upload is already paused, this method does nothing.
     */
    suspend fun pause()

    /**
     * Cancels this upload and removes the upload url from the cache.
     */
    suspend fun cancel()

    /**
     * Starts or resumes this upload. Location url may be retrieved from the cache, so can start of after your program crashed or the network reconnected.
     */
    suspend fun startOrResumeUploading()

}

class ResumableUploadImpl(
    private val path: String,
    private val dataStream: ByteReadChannel,
    private val size: Long,
    private var offset: Long,
    private val chunkSize: Long,
    private val locationUrl: String,
    private val httpClient: HttpClient,
    private val storageApi: BucketApi,
    private val removeFromCache: suspend () -> Unit
): ResumableUpload {

    private var serverOffset = 0L
    private var paused = false
    private val mutex = Mutex()
    private val _progressFlow = MutableStateFlow<UploadStatus>(UploadStatus.Progress(offset, size))
    val progressFlow: StateFlow<UploadStatus> = _progressFlow.asStateFlow()
    private val scope = CoroutineScope(Dispatchers.Default)

    override suspend fun pause() {
        mutex.withLock {
            paused = true
        }
    }

    override suspend fun cancel() {
        scope.cancel()
        removeFromCache()
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun startOrResumeUploading() {
        scope.launch {
            if(paused) {
                mutex.withLock {
                    paused = false
                }
            }
            while (offset < size) {
                if(paused) return@launch
                offset += uploadChunk()
                _progressFlow.value = UploadStatus.Progress(offset, size)
            }
            if(offset != serverOffset) error("Upload offset does not match server offset")
            _progressFlow.value = UploadStatus.Success(path)
            removeFromCache()
        }
    }

    @OptIn(SupabaseInternal::class)
    private suspend fun uploadChunk(): Int {
        val limit = min(chunkSize, size.toInt() - offset)
        val buffer = ByteArray(limit.toInt())
        var totalRead = 0
        var read: Int

        while (totalRead < limit.toInt()) {
            read = dataStream.readAvailable(buffer, totalRead, limit.toInt() - totalRead)
            if (read == -1) {
                break
            }
            totalRead += read
        }
        val uploadResponse = httpClient.patch(locationUrl) {
            header("Tus-Resumable", TUS_VERSION)
            header("Content-Type", "application/offset+octet-stream")
            header("Upload-Offset", offset)
            bearerAuth(accessTokenOrApiKey())
            setBody(StreamContent(totalRead.toLong()) {
                writeFully(buffer, 0, totalRead)
            })
        }
        when(uploadResponse.status) {
            HttpStatusCode.NoContent -> {
                serverOffset = uploadResponse.headers["Upload-Offset"]?.toLong() ?: error("No upload offset found")
            }
            HttpStatusCode.Conflict -> {
                Napier.w { "Upload conflict, skipping chunk" }
                serverOffset = offset + totalRead
            }
        }
        return totalRead
    }

    private fun accessTokenOrApiKey() = storageApi.supabaseClient.pluginManager.getPluginOrNull(GoTrue)?.currentAccessTokenOrNull() ?: storageApi.supabaseClient.supabaseKey

}
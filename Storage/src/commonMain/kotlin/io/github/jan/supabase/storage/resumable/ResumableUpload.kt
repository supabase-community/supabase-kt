package io.github.jan.supabase.storage.resumable

import io.github.aakira.napier.Napier
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.UploadStatus
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
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

class StreamContent(
    size: Long,
    private val copyTo: suspend ByteWriteChannel.() -> Unit
) : OutgoingContent.WriteChannelContent() {

    override val contentLength: Long = size
    override val contentType: ContentType = ContentType.parse("application/offset+octet-stream")

    override suspend fun writeTo(channel: ByteWriteChannel) {
        copyTo(channel)
    }

}


class ResumableUpload(
    private val path: String,
    private val dataStream: ByteReadChannel,
    private val size: Long,
    private var offset: Long,
    private val chunkSize: Long,
    private val locationUrl: String,
    private val httpClient: HttpClient,
    private val storageApi: BucketApi,
    private val removeFromCache: suspend () -> Unit
) {

    private var serverOffset = 0L
    private var paused = false
    private val mutex = Mutex()
    private val _progressFlow = MutableStateFlow<UploadStatus>(UploadStatus.Progress(offset, size))
    val progressFlow: StateFlow<UploadStatus> = _progressFlow.asStateFlow()
    private val scope = CoroutineScope(Dispatchers.Default)

    suspend fun pause() {
        mutex.withLock {
            paused = true
        }
    }

    suspend fun cancel() {
        scope.cancel()
        removeFromCache()
    }

    @OptIn(ExperimentalTime::class)
    suspend fun startOrResumeUploadingChunks() {
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
            header("Tus-Resumable", "1.0.0")
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
                Napier.d { "Upload confict, skipping chunk" }
                serverOffset = offset + totalRead
            }
        }
        return totalRead
    }

    private fun accessTokenOrApiKey() = storageApi.supabaseClient.pluginManager.getPluginOrNull(GoTrue)?.currentAccessTokenOrNull() ?: storageApi.supabaseClient.supabaseKey

}
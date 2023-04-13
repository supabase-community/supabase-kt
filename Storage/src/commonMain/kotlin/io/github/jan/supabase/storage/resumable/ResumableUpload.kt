package io.github.jan.supabase.storage.resumable

import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.network.KtorSupabaseHttpClient
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.UploadStatus
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
    private val dataStream: ByteReadChannel,
    private val size: Long,
    private val locationUrl: String,
    private val httpClient: KtorSupabaseHttpClient,
    private val storageApi: BucketApi
) {

    private var offset = 0
    private var serverOffset = 0
    private var paused = false
    private val mutex = Mutex()
    private val _progressFlow = MutableStateFlow<UploadStatus>(UploadStatus.Progress(0, size))
    val progressFlow: StateFlow<UploadStatus> = _progressFlow.asStateFlow()
    private val scope = CoroutineScope(Dispatchers.Default)

    suspend fun pause() {
        mutex.withLock {
            paused = true
        }
    }

    suspend fun cancel() {
        scope.cancel()
    }

    suspend fun startOrResumeUploadingChunks() {
        if(paused) {
            mutex.withLock {
                paused = false
            }
        }
        scope.launch {
            while (offset < size) {
                if(paused) return@launch
                offset += uploadChunk()
                _progressFlow.value = UploadStatus.Progress(offset.toLong(), size)
            }
            if(offset != serverOffset) error("Upload offset does not match server offset")
            _progressFlow.value = UploadStatus.Success("Upload finished successfully")
        }
    }

    private suspend fun uploadChunk(): Int {
        val limit = min(6 * 1024, size.toInt() - offset)
        val buffer = ByteArray(limit)
        val read = dataStream.readAvailable(buffer, 0, limit)
        val uploadResponse = httpClient.httpClient.patch(locationUrl) {
            header("Tus-Resumable", "1.0.0")
            header("Content-Type", "application/offset+octet-stream")
            header("Upload-Offset", offset)
            bearerAuth(accessTokenOrApiKey())
            setBody(StreamContent(read.toLong()) {
                writeFully(buffer, 0, read)
            })
        }
        serverOffset = uploadResponse.headers["Upload-Offset"]?.toInt() ?: error("No upload offset found")
        return read
    }

    private fun accessTokenOrApiKey() = storageApi.supabaseClient.pluginManager.getPluginOrNull(GoTrue)?.currentAccessTokenOrNull() ?: storageApi.supabaseClient.supabaseKey

}
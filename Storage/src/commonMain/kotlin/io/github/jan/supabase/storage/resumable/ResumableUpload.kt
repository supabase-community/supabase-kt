package io.github.jan.supabase.storage.resumable

import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.network.KtorSupabaseHttpClient
import io.github.jan.supabase.storage.BucketApi
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlin.math.min

class StreamContent(
    size: Long,
    private val copyTo: suspend ByteWriteChannel.() -> Unit
) : OutgoingContent.WriteChannelContent() {

    override val contentLength: Long = size
    override val contentType: ContentType = ContentType.Application.OctetStream

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

    suspend fun uploadChunksUntilFinished() {
        while (offset < size) {
            offset += uploadChunk()
            println("offset: $offset")
        }
    }

    private suspend fun uploadChunk(): Int {
        val limit = min(6 * 1024 * 1024, size.toInt() - offset)
        //write max 1024 bytes to 'this' WriteChannel using the offset as starting point. Use a buffer to avoid blocking
        val buffer = ByteArray(limit)
        val read = dataStream.readAvailable(buffer, 0, limit)
        val uploadResponse = httpClient.httpClient.patch(locationUrl) {
            header("Tus-Resumable", "1.0.0")
            header("Content-Type", ContentType.Application.OctetStream.toString())
            header("Upload-Offset", offset)
            bearerAuth(accessTokenOrApiKey())
      //      header("Expect", "100-continue")
            setBody(StreamContent(read.toLong()) {
                writeFully(buffer, 0, read)
            })
        }
        println(offset)
        println(uploadResponse.bodyAsText())
        println(uploadResponse.status)
        println(uploadResponse.headers)
        return read
    }

    private fun accessTokenOrApiKey() = storageApi.supabaseClient.pluginManager.getPluginOrNull(GoTrue)?.currentAccessTokenOrNull() ?: storageApi.supabaseClient.supabaseKey

}
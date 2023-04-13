package io.github.jan.supabase.storage.resumable

import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.storage.BucketApi
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class ResumableClient(private val storageApi: BucketApi, private val cache: ResumableCache) {

    private val httpClient = storageApi.supabaseClient.httpClient
    private val url = "http://localhost:1080"//storageApi.supabaseClient.storage.resolveUrl("upload/resumable")

    suspend fun startOrResumeDownload(data: ByteArray, path: String): ResumableUpload {
        val response = httpClient.post(url) {
            header("Upload-Metadata", encodeMetadata(createMetadata(path)))
            bearerAuth(accessTokenOrApiKey())
            header("Upload-Length", data.size)
            header("Tus-Resumable", "1.0.0")
        }
        println(response.status)
        println(response.bodyAsText())
        val uploadUrl = response.headers["Location"] ?: error("No upload url found")
        return ResumableUpload(ByteReadChannel(data), data.size.toLong(), uploadUrl, httpClient, storageApi)
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

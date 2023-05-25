package io.github.jan.supabase.storage

import io.github.jan.supabase.annotiations.SupabaseExperimental
import io.github.jan.supabase.putJsonObject
import io.github.jan.supabase.safeBody
import io.github.jan.supabase.storage.resumable.ResumableClientImpl
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.http.content.OutgoingContent
import io.ktor.http.defaultForFilePath
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.close
import io.ktor.utils.io.copyTo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlin.time.Duration

internal class BucketApiImpl(override val bucketId: String, val storage: StorageImpl) : BucketApi {

    override val supabaseClient = storage.supabaseClient
    @SupabaseExperimental
    override val resumable = ResumableClientImpl(this, storage.config.resumable.cache)

    override suspend fun update(path: String, data: UploadData, upsert: Boolean): String = uploadOrUpdate(
        HttpMethod.Put, bucketId, path, data, upsert)

    @SupabaseExperimental
    override fun updateAsFlow(path: String, data: UploadData, upsert: Boolean): Flow<UploadStatus> = callbackFlow {
        val key = uploadOrUpdate(HttpMethod.Put, bucketId, path, data, upsert) {
            onUpload { bytesSentTotal, contentLength ->
                trySend(UploadStatus.Progress(bytesSentTotal, contentLength))
            }
        }
        trySend(UploadStatus.Success(key))
        close()
    }

    override suspend fun uploadToSignedUrl(path: String, token: String, data: UploadData, upsert: Boolean): String {
        return uploadToSignedUrl(path, token, data, upsert) {}
    }

    @SupabaseExperimental
    override fun uploadToSignedUrlAsFlow(
        path: String,
        token: String,
        data: UploadData,
        upsert: Boolean
    ): Flow<UploadStatus> {
        return callbackFlow {
            val key = uploadToSignedUrl(path, token, data, upsert) {
                onUpload { bytesSentTotal, contentLength ->
                    trySend(UploadStatus.Progress(bytesSentTotal, contentLength))
                }
            }
            trySend(UploadStatus.Success(key))
            close()
        }
    }

    override suspend fun createUploadSignedUrl(path: String): UploadSignedUrl {
        val result = storage.api.post("object/upload/sign/$bucketId/$path")
        val urlPath = result.body<JsonObject>()["url"]?.jsonPrimitive?.content ?: error("Expected a url in create upload signed url response")
        val url = Url(storage.resolveUrl(urlPath))
        return UploadSignedUrl(
            url = url.toString(),
            path = path,
            token = url.parameters["token"] ?: error("Expected a token in create upload signed url response")
        )
    }

    override suspend fun upload(path: String, data: UploadData, upsert: Boolean): String = uploadOrUpdate(
        HttpMethod.Post, bucketId, path, data, upsert)

    @SupabaseExperimental
    override fun uploadAsFlow(path: String, data: UploadData, upsert: Boolean): Flow<UploadStatus> {
        return callbackFlow {
            val key = uploadOrUpdate(HttpMethod.Post, bucketId, path, data, upsert) {
                onUpload { bytesSentTotal, contentLength ->
                    trySend(UploadStatus.Progress(bytesSentTotal, contentLength))
                }
            }
            trySend(UploadStatus.Success(key))
            close()
        }
    }

    override suspend fun delete(paths: Collection<String>) {
        storage.api.deleteJson("object/$bucketId", buildJsonObject {
            putJsonArray("prefixes") {
                paths.forEach(this::add)
            }
        })
    }

    override suspend fun move(from: String, to: String) {
        storage.api.postJson("object/move", buildJsonObject {
            put("bucketId", bucketId)
            put("sourceKey", from)
            put("destinationKey", to)
        })
    }

    override suspend fun copy(from: String, to: String) {
        storage.api.postJson("object/copy", buildJsonObject {
            put("bucketId", bucketId)
            put("sourceKey", from)
            put("destinationKey", to)
        })
    }

    override suspend fun createSignedUrl(
        path: String,
        expiresIn: Duration,
        transform: ImageTransformation.() -> Unit
    ): String {
        val transformation = ImageTransformation().apply(transform)
        val body = storage.api.postJson("object/sign/$bucketId/$path", buildJsonObject {
            put("expiresIn", expiresIn.inWholeSeconds)
            transformation.width?.let { put("width", it) }
            transformation.height?.let { put("height", it) }
            transformation.resize?.let { put("resize", it.name.lowercase()) }
        }).body<JsonObject>()
        return body["signedURL"]?.jsonPrimitive?.content?.substring(1)
            ?: error("Expected signed url in response")
    }

    override suspend fun createSignedUrls(expiresIn: Duration, paths: Collection<String>): List<SignedUrl> {
        val body = storage.api.postJson("object/sign/$bucketId", buildJsonObject {
            putJsonArray("paths") {
                paths.forEach(this::add)
            }
            put("expiresIn", expiresIn.inWholeSeconds)
        }).body<List<SignedUrl>>().map {
            it.copy(signedURL = storage.resolveUrl(it.signedURL.substring(1)))
        }
        return body
    }

    override suspend fun downloadAuthenticated(path: String, transform: ImageTransformation.() -> Unit): ByteArray {
        return storage.api.rawRequest {
            prepareDownloadRequest(path, false, transform)
        }.body()
    }

    @SupabaseExperimental
    override fun downloadAuthenticatedAsFlow(
        path: String,
        transform: ImageTransformation.() -> Unit
    ): Flow<DownloadStatus> {
        return callbackFlow {
            val data = storage.api.rawRequest {
                prepareDownloadRequest(path, false, transform)
                onDownload { bytesSentTotal, contentLength ->
                    trySend(DownloadStatus.Progress(bytesSentTotal, contentLength))
                }
            }.body<ByteArray>()
            trySend(DownloadStatus.Success)
            trySend(DownloadStatus.ByteData(data))
            close()
        }
    }

    override suspend fun downloadPublic(path: String, transform: ImageTransformation.() -> Unit): ByteArray {
        return storage.api.rawRequest {
            prepareDownloadRequest(path, true, transform)
        }.body()
    }

    @SupabaseExperimental
    override fun downloadPublicAsFlow(path: String, transform: ImageTransformation.() -> Unit): Flow<DownloadStatus> {
        return callbackFlow {
            val data = storage.api.rawRequest {
                prepareDownloadRequest(path, true, transform)
                onDownload { bytesSentTotal, contentLength ->
                    trySend(DownloadStatus.Progress(bytesSentTotal, contentLength))
                }
            }.body<ByteArray>()
            trySend(DownloadStatus.Success)
            trySend(DownloadStatus.ByteData(data))
            close()
        }
    }

    override suspend fun downloadAuthenticated(
        path: String,
        channel: ByteWriteChannel,
        transform: ImageTransformation.() -> Unit
    ) {
        channelDownloadRequest(path, channel, false, transform)
    }

    @SupabaseExperimental
    override fun downloadAuthenticatedAsFlow(
        path: String,
        channel: ByteWriteChannel,
        transform: ImageTransformation.() -> Unit
    ): Flow<DownloadStatus> = flowChannelDownloadRequest(path, channel, false, transform)

    override suspend fun downloadPublic(
        path: String,
        channel: ByteWriteChannel,
        transform: ImageTransformation.() -> Unit
    ) {
        channelDownloadRequest(path, channel, true, transform)
    }

    @SupabaseExperimental
    override fun downloadPublicAsFlow(
        path: String,
        channel: ByteWriteChannel,
        transform: ImageTransformation.() -> Unit
    ): Flow<DownloadStatus> = flowChannelDownloadRequest(path, channel, true, transform)

    private fun flowChannelDownloadRequest(path: String, channel: ByteWriteChannel, public: Boolean, transform: ImageTransformation.() -> Unit): Flow<DownloadStatus> = callbackFlow {
        channelDownloadRequest(path, channel, public, transform) {
            onDownload { bytesSentTotal, contentLength ->
                trySend(DownloadStatus.Progress(bytesSentTotal, contentLength))
            }
        }
        trySend(DownloadStatus.Success)
        close()
    }

    private suspend fun channelDownloadRequest(path: String, channel: ByteWriteChannel, public: Boolean, transform: ImageTransformation.() -> Unit, extra: HttpRequestBuilder.() -> Unit = {}) {
        storage.api.prepareRequest {
            prepareDownloadRequest(path, public, transform)
            extra()
        }.execute {
            it.bodyAsChannel().copyTo(channel)
        }
        channel.close()
    }

    private fun HttpRequestBuilder.prepareDownloadRequest(path: String, public: Boolean, transform: ImageTransformation.() -> Unit) {
        val transformation = ImageTransformation().apply(transform).queryString()
        val url = when(public) {
            true -> if(transformation.isBlank()) publicUrl(path) else publicRenderUrl(path, transform)
            false -> if(transformation.isBlank()) authenticatedUrl(path) else authenticatedRenderUrl(path, transform)
        }
        method = HttpMethod.Get
        url(url)
    }

    override suspend fun list(prefix: String, filter: BucketListFilter.() -> Unit): List<BucketItem> {
        return storage.api.postJson("object/list/$bucketId", buildJsonObject {
            put("prefix", prefix)
            putJsonObject(BucketListFilter().apply(filter).build())
        }).safeBody()
    }

    private suspend fun uploadOrUpdate(method: HttpMethod, bucket: String, path: String, data: UploadData, upsert: Boolean, extra: HttpRequestBuilder.() -> Unit = {}): String {
        return storage.api.request("object/$bucket/$path") {
            this.method = method
            setBody(object : OutgoingContent.ReadChannelContent() {
                override val contentType: ContentType = ContentType.defaultForFilePath(path)
                override val contentLength: Long = data.size
                override fun readFrom(): ByteReadChannel = data.stream
            })
            header(HttpHeaders.ContentType, ContentType.defaultForFilePath(path))
            header("x-upsert", upsert.toString())
            extra()
        }.body<JsonObject>()["Key"]?.jsonPrimitive?.content
            ?: error("Expected a key in a upload response")
    }

    private suspend fun uploadToSignedUrl(path: String, token: String, data: UploadData, upsert: Boolean, extra: HttpRequestBuilder.() -> Unit = {}): String {
        return storage.api.put("object/upload/sign/$bucketId/$path") {
            parameter("token", token)
            setBody(object : OutgoingContent.ReadChannelContent() {
                override val contentType: ContentType = ContentType.defaultForFilePath(path)
                override val contentLength: Long = data.size
                override fun readFrom(): ByteReadChannel = data.stream
            })
            header(HttpHeaders.ContentType, ContentType.defaultForFilePath(path))
            header("x-upsert", upsert.toString())
            extra()
        }.body<JsonObject>()["Key"]?.jsonPrimitive?.content ?: error("Expected a key in a upload response")
    }

    override suspend fun changePublicStatusTo(public: Boolean) = storage.changePublicStatus(bucketId, public)

    override fun authenticatedUrl(path: String): String = storage.resolveUrl("object/authenticated/$bucketId/$path")

    override fun publicUrl(path: String): String = storage.resolveUrl("object/public/$bucketId/$path")

    override fun authenticatedRenderUrl(path: String, transform: ImageTransformation.() -> Unit): String {
        val transformation = ImageTransformation().apply(transform).queryString()
        return storage.resolveUrl("render/image/authenticated/$bucketId/$path${if(transformation.isNotBlank()) "?$transformation" else ""}")
    }

    override fun publicRenderUrl(path: String, transform: ImageTransformation.() -> Unit): String {
        val transformation = ImageTransformation().apply(transform).queryString()
        return storage.resolveUrl("render/image/public/$bucketId/$path${if(transformation.isNotBlank()) "?$transformation" else ""}")
    }

}

package io.github.jan.supabase.storage

import io.github.jan.supabase.putJsonObject
import io.github.jan.supabase.safeBody
import io.github.jan.supabase.storage.BucketApi.Companion.UPSERT_HEADER
import io.github.jan.supabase.storage.resumable.ResumableCache
import io.github.jan.supabase.storage.resumable.ResumableClientImpl
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.time.Duration

internal class BucketApiImpl(override val bucketId: String, val storage: StorageImpl, resumableCache: ResumableCache) : BucketApi {

    override val supabaseClient = storage.supabaseClient

    override val resumable = ResumableClientImpl(this, resumableCache)

    override suspend fun update(
        path: String,
        data: UploadData,
        options: UploadOptionBuilder.() -> Unit
    ): FileUploadResponse =
        uploadOrUpdate(
            HttpMethod.Put, defaultUploadUrl(path), data, options
        )

    override suspend fun uploadToSignedUrl(
        path: String,
        token: String,
        data: UploadData,
        options: UploadOptionBuilder.() -> Unit
    ): FileUploadResponse {
        return uploadOrUpdate(
            HttpMethod.Put, uploadToSignedUrlUrl(path, token), data, options
        )
    }

    override suspend fun createSignedUploadUrl(path: String): UploadSignedUrl {
        val result = storage.api.post("object/upload/sign/$bucketId/$path")
        val urlPath = result.body<JsonObject>()["url"]?.jsonPrimitive?.content?.substring(1)
            ?: error("Expected a url in create upload signed url response")
        val url = Url(storage.resolveUrl(urlPath))
        return UploadSignedUrl(
            url = url.toString(),
            path = path,
            token = url.parameters["token"]
                ?: error("Expected a token in create upload signed url response")
        )
    }

    override suspend fun upload(
        path: String,
        data: UploadData,
        options: UploadOptionBuilder.() -> Unit
    ): FileUploadResponse =
        uploadOrUpdate(
            HttpMethod.Post, defaultUploadUrl(path), data, options
        )

    override suspend fun delete(paths: Collection<String>) {
        storage.api.deleteJson("object/$bucketId", buildJsonObject {
            putJsonArray("prefixes") {
                paths.forEach(this::add)
            }
        })
    }

    override suspend fun move(from: String, to: String, destinationBucket: String?) {
        storage.api.postJson("object/move", buildJsonObject {
            put("bucketId", bucketId)
            put("sourceKey", from)
            put("destinationKey", to)
            destinationBucket?.let { put("destinationBucket", it) }
        })
    }

    override suspend fun copy(from: String, to: String, destinationBucket: String?) {
        storage.api.postJson("object/copy", buildJsonObject {
            put("bucketId", bucketId)
            put("sourceKey", from)
            put("destinationKey", to)
            destinationBucket?.let { put("destinationBucket", it) }
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
            putJsonObject("transform") {
                putImageTransformation(transformation)
            }
        }).body<JsonObject>()
        return storage.resolveUrl(body["signedURL"]?.jsonPrimitive?.content?.substring(1)
            ?: error("Expected signed url in response"))
    }

    override suspend fun createSignedUrls(
        expiresIn: Duration,
        paths: Collection<String>
    ): List<SignedUrl> {
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

    override suspend fun downloadAuthenticated(
        path: String,
        options: DownloadOptionBuilder.() -> Unit
    ): ByteArray = normalDownloadRequest(path, false, options)


    override suspend fun downloadPublic(
        path: String,
        options: DownloadOptionBuilder.() -> Unit
    ): ByteArray = normalDownloadRequest(path, true, options)

    private suspend fun normalDownloadRequest(
        path: String,
        public: Boolean,
        options: DownloadOptionBuilder.() -> Unit
    ): ByteArray {
        val downloadOptions = DownloadOptionBuilder().apply(options)
        return storage.api.rawRequest {
            prepareDownloadRequest(path, public, downloadOptions)
            downloadOptions.httpRequestOverrides.forEach { it() }
        }.body()
    }

    override suspend fun downloadAuthenticated(
        path: String,
        channel: ByteWriteChannel,
        options: DownloadOptionBuilder.() -> Unit
    ) {
        channelDownloadRequest(path, channel, false, options)
    }


    override suspend fun downloadPublic(
        path: String,
        channel: ByteWriteChannel,
        options: DownloadOptionBuilder.() -> Unit
    ) {
        channelDownloadRequest(path, channel, true, options)
    }

    internal suspend fun channelDownloadRequest(
        path: String,
        channel: ByteWriteChannel,
        public: Boolean,
        options: DownloadOptionBuilder.() -> Unit,
    ) {
        val downloadOptions = DownloadOptionBuilder().apply(options)
        storage.api.prepareRequest {
            prepareDownloadRequest(path, public, downloadOptions)
            downloadOptions.httpRequestOverrides.forEach { it() }
        }.execute {
            it.bodyAsChannel().copyTo(channel)
        }
        channel.close()
    }

    internal fun HttpRequestBuilder.prepareDownloadRequest(
        path: String,
        public: Boolean,
        options: DownloadOptionBuilder
    ) {
        val transformation = ImageTransformation().apply(options.transform).queryString()
        val url = when (public) {
            true -> if (transformation.isBlank()) publicUrl(path) else publicRenderUrl(
                path,
                options.transform
            )
            false -> if (transformation.isBlank()) authenticatedUrl(path) else authenticatedRenderUrl(
                path,
                options.transform
            )
        }
        method = HttpMethod.Get
        url(url)
    }

    override suspend fun list(
        prefix: String,
        filter: BucketListFilter.() -> Unit
    ): List<FileObject> {
        return storage.api.postJson("object/list/$bucketId", buildJsonObject {
            put("prefix", prefix)
            putJsonObject(BucketListFilter().apply(filter).build())
        }).safeBody()
    }

    private fun defaultUploadUrl(path: String) = "object/$bucketId/$path"

    private fun uploadToSignedUrlUrl(path: String, token: String) = "object/upload/sign/$bucketId/$path?token=$token"

    internal suspend fun uploadOrUpdate(
        method: HttpMethod,
        url: String,
        data: UploadData,
        options: UploadOptionBuilder.() -> Unit,
    ): FileUploadResponse {
        val path = url.substringAfterLast('/').substringBeforeLast("?")
        val optionBuilder = UploadOptionBuilder(storage.serializer).apply(options)
        val response = storage.api.request(url) {
            this.method = method
            defaultUploadRequest(path, data, optionBuilder)
            optionBuilder.httpRequestOverrides.forEach { it() }
        }.body<JsonObject>()
        val key = response["Key"]?.jsonPrimitive?.content
            ?: error("Expected a key in a upload response")
        val id = response["Id"]?.jsonPrimitive?.content
            ?: error("Expected an id in a upload response")
        return FileUploadResponse(id, path, key)
    }

    private fun HttpRequestBuilder.defaultUploadRequest(
        path: String,
        data: UploadData,
        optionBuilder: UploadOptionBuilder,
    ) {
        setBody(object : OutgoingContent.ReadChannelContent() {
            override val contentType: ContentType = optionBuilder.contentType ?: ContentType.defaultForFilePath(path)
            override val contentLength: Long = data.size
            override fun readFrom(): ByteReadChannel = data.stream
        })
        header(HttpHeaders.ContentType, optionBuilder.contentType ?: ContentType.defaultForFilePath(path))
        header(UPSERT_HEADER, optionBuilder.upsert.toString())
    }

    override suspend fun changePublicStatusTo(public: Boolean) = storage.updateBucket(bucketId) {
        this@updateBucket.public = public
    }

    override fun authenticatedUrl(path: String): String =
        storage.resolveUrl("object/authenticated/$bucketId/$path")

    override fun publicUrl(path: String): String =
        storage.resolveUrl("object/public/$bucketId/$path")

    override fun authenticatedRenderUrl(
        path: String,
        transform: ImageTransformation.() -> Unit
    ): String {
        val transformation = ImageTransformation().apply(transform).queryString()
        return storage.resolveUrl("render/image/authenticated/$bucketId/$path${if (transformation.isNotBlank()) "?$transformation" else ""}")
    }

    override fun publicRenderUrl(path: String, transform: ImageTransformation.() -> Unit): String {
        val transformation = ImageTransformation().apply(transform).queryString()
        return storage.resolveUrl("render/image/public/$bucketId/$path${if (transformation.isNotBlank()) "?$transformation" else ""}")
    }

}

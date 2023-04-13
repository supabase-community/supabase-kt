package io.github.jan.supabase.storage

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotiations.SupabaseExperimental
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.putJsonObject
import io.github.jan.supabase.safeBody
import io.github.jan.supabase.storage.resumable.ResumableClient
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.*
import kotlin.time.Duration

sealed interface BucketApi {

    val bucketId: String
    val supabaseClient: SupabaseClient
    val resumableClient: ResumableClient

    /**
     * Uploads a file in [bucketId] under [path]
     * @param path The path to upload the file to
     * @param data The data to upload
     * @param upsert Whether to overwrite an existing file
     * @return the key to the uploaded file
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun upload(path: String, data: ByteArray, upsert: Boolean = false): String

    /**
     * Uploads a file in [bucketId] under [path]
     * @param path The path to upload the file to
     * @param upsert Whether to overwrite an existing file
     * @param data The data to upload
     * @return A flow that emits the upload progress and at last the key to the uploaded file
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    @SupabaseExperimental
    suspend fun uploadAsFlow(path: String, data: ByteArray, upsert: Boolean = false): Flow<UploadStatus>

    /**
     * Uploads a file in [bucketId] under [path] using a presigned url
     * @param path The path to upload the file to
     * @param token The presigned url token
     * @param data The data to upload
     * @param upsert Whether to overwrite an existing file
     * @return the key of the uploaded file
     */
    suspend fun uploadToSignedUrl(path: String, token: String, data: ByteArray, upsert: Boolean = false): String

    /**
     * Uploads a file in [bucketId] under [path] using a presigned url
     * @param path The path to upload the file to
     * @param token The presigned url token
     * @param data The data to upload
     * @param upsert Whether to overwrite an existing file
     * @return A flow that emits the upload progress and at last the key to the uploaded file
     */
    @SupabaseExperimental
    suspend fun uploadToSignedUrlAsFlow(path: String, token: String, data: ByteArray, upsert: Boolean = false): Flow<UploadStatus>

    /**
     * Updates a file in [bucketId] under [path]
     * @param path The path to update the file to
     * @param data The new data
     * @param upsert Whether to overwrite an existing file
     * @return the key to the updated file
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun update(path: String, data: ByteArray, upsert: Boolean = false): String

    /**
     * Updates a file in [bucketId] under [path]
     * @param path The path to update the file to
     * @param data The new data
     * @param upsert Whether to overwrite an existing file
     * @return A flow that emits the upload progress and at last the key to the uploaded file
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    @SupabaseExperimental
    suspend fun updateAsFlow(path: String, data: ByteArray, upsert: Boolean = false): Flow<UploadStatus>

    /**
     * Deletes all files in [bucketId] with in [paths]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun delete(paths: Collection<String>)

    /**
     * Deletes all files in [bucketId] with in [paths]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun delete(vararg paths: String) = delete(paths.toList())

    /**
     * Moves a file under [from] to [to]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun move(from: String, to: String)

    /**
     * Copies a file under [from] to [to]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun copy(from: String, to: String)

    /**
     * Creates a signed url to upload without authentication.
     * @param path The path to create an url for
     */
    suspend fun createUploadSignedUrl(path: String): UploadSignedUrl

    /**
     * Creates a signed url to download without authentication. The url will expire after [expiresIn]
     * @param path The path to create an url for
     * @param expiresIn The duration the url is valid
     * @param transform The transformation to apply to the image
     * @return The url to download the file
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun createSignedUrl(path: String, expiresIn: Duration, transform: ImageTransformation.() -> Unit = {}): String

    /**
     * Creates signed urls for all specified paths. The urls will expire after [expiresIn]
     * @param expiresIn The duration the urls are valid
     * @param paths The paths to create urls for
     * @return A list of [SignedUrl]s
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun createSignedUrls(expiresIn: Duration, paths: Collection<String>): List<SignedUrl>

    /**
     * Creates signed urls for all specified paths. The urls will expire after [expiresIn]
     * @param expiresIn The duration the urls are valid
     * @param paths The paths to create urls for
     * @return A list of [SignedUrl]s
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun createSignedUrls(expiresIn: Duration, vararg paths: String) = createSignedUrls(expiresIn, paths.toList())

    /**
     * Downloads a file from [bucketId] under [path]
     * @param path The path to download
     * @param transform The transformation to apply to the image
     * @return The file as a byte array
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun downloadAuthenticated(path: String, transform: ImageTransformation.() -> Unit = {}): ByteArray

    /**
     * Downloads a file from [bucketId] under [path]
     * @param path The path to download
     * @param transform The transformation to apply to the image
     * @return A flow that emits the download progress and at last the data as a byte array
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    @SupabaseExperimental
    suspend fun downloadAuthenticatedAsFlow(path: String, transform: ImageTransformation.() -> Unit = {}): Flow<DownloadStatus>

    /**
     * Downloads a file from [bucketId] under [path] using the public url
     * @param path The path to download
     * @param transform The transformation to apply to the image
     * @return The file as a byte array
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun downloadPublic(path: String, transform: ImageTransformation.() -> Unit = {}): ByteArray

    /**
     * Downloads a file from [bucketId] under [path] using the public url
     * @param path The path to download
     * @param transform The transformation to apply to the image
     * @return A flow that emits the download progress and at last the data as a byte array
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    @SupabaseExperimental
    suspend fun downloadPublicAsFlow(path: String, transform: ImageTransformation.() -> Unit = {}): Flow<DownloadStatus>

    /**
     * Searches for buckets with the given [prefix] and [filter]
     * @return The filtered buckets
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun list(prefix: String, filter: BucketListFilter.() -> Unit = {}): List<BucketItem>

    /**
     * Changes the bucket's public status to [public]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun changePublicStatusTo(public: Boolean)

    /**
     * Returns the public url of [path]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    fun publicUrl(path: String): String

    /**
     * Returns the authenticated url of [path]. Requires bearer token authentication using the user's access token
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    fun authenticatedUrl(path: String): String

    /**
     * Returns the authenticated render url of [path] with the given [transform]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    fun authenticatedRenderUrl(path: String, transform: ImageTransformation.() -> Unit = {}): String

    /**
     * Returns the public render url of [path] with the given [transform]
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    fun publicRenderUrl(path: String, transform: ImageTransformation.() -> Unit = {}): String
    
}

internal class BucketApiImpl(override val bucketId: String, val storage: StorageImpl) : BucketApi {

    override val supabaseClient = storage.supabaseClient
    override val resumableClient = ResumableClient(this)

    override suspend fun update(path: String, data: ByteArray, upsert: Boolean): String = uploadOrUpdate(HttpMethod.Put, bucketId, path, data, upsert)

    @SupabaseExperimental
    override suspend fun updateAsFlow(path: String, data: ByteArray, upsert: Boolean): Flow<UploadStatus> = callbackFlow {
        val key = uploadOrUpdate(HttpMethod.Put, bucketId, path, data, upsert) {
            onUpload { bytesSentTotal, contentLength ->
                trySend(UploadStatus.Progress(bytesSentTotal, contentLength))
            }
        }
        trySend(UploadStatus.Success(key))
        close()
    }

    override suspend fun uploadToSignedUrl(path: String, token: String, data: ByteArray, upsert: Boolean): String {
        return uploadToSignedUrl(path, token, data, upsert) {}
    }

    @SupabaseExperimental
    override suspend fun uploadToSignedUrlAsFlow(
        path: String,
        token: String,
        data: ByteArray,
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
        val urlPath = result.body<JsonObject>()["url"]?.jsonPrimitive?.content ?: throw IllegalStateException("Expected a url in create upload signed url response")
        val url = Url(storage.resolveUrl(urlPath))
        return UploadSignedUrl(
            url = url.toString(),
            path = path,
            token = url.parameters["token"] ?: throw IllegalStateException("Expected a token in create upload signed url response")
        )
    }

    override suspend fun upload(path: String, data: ByteArray, upsert: Boolean): String = uploadOrUpdate(HttpMethod.Post, bucketId, path, data, upsert)

    @SupabaseExperimental
    override suspend fun uploadAsFlow(path: String, data: ByteArray, upsert: Boolean): Flow<UploadStatus> {
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
            ?: throw IllegalStateException("Expected signed url in response")
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
    override suspend fun downloadAuthenticatedAsFlow(
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
            trySend(DownloadStatus.Success(data))
            close()
        }
    }

    override suspend fun downloadPublic(path: String, transform: ImageTransformation.() -> Unit): ByteArray {
        return storage.api.rawRequest {
            prepareDownloadRequest(path, true, transform)
        }.body()
    }

    @SupabaseExperimental
    override suspend fun downloadPublicAsFlow(path: String, transform: ImageTransformation.() -> Unit): Flow<DownloadStatus> {
        return callbackFlow {
            val data = storage.api.rawRequest {
                prepareDownloadRequest(path, true, transform)
                onDownload { bytesSentTotal, contentLength ->
                    trySend(DownloadStatus.Progress(bytesSentTotal, contentLength))
                }
            }.body<ByteArray>()
            trySend(DownloadStatus.Success(data))
            close()
        }
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

    private suspend fun uploadOrUpdate(method: HttpMethod, bucket: String, path: String, body: ByteArray, upsert: Boolean, extra: HttpRequestBuilder.() -> Unit = {}): String {
        return storage.api.request("object/$bucket/$path") {
            this.method = method
            setBody(body)
            header(HttpHeaders.ContentType, ContentType.defaultForFilePath(path))
            header("x-upsert", upsert.toString())
            extra()
        }.body<JsonObject>()["Key"]?.jsonPrimitive?.content ?: throw IllegalStateException("Expected a key in a upload response")
    }

    private suspend fun uploadToSignedUrl(path: String, token: String, body: ByteArray, upsert: Boolean, extra: HttpRequestBuilder.() -> Unit = {}): String {
        return storage.api.put("object/upload/sign/$bucketId/$path") {
            parameter("token", token)
            setBody(body)
            header(HttpHeaders.ContentType, ContentType.defaultForFilePath(path))
            header("x-upsert", upsert.toString())
            extra()
        }.body<JsonObject>()["Key"]?.jsonPrimitive?.content ?: throw IllegalStateException("Expected a key in a upload response")
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

/**
 * Can be used if you want to quickly access a file under an **url** with your **auth_token** using a custom download method.
 *
 *
 * To interact with files which require authentication use the provided access token and add it to the Authorization header:
 *
 * **Authentication: Bearer <your_access_token>**
 * @param path The path to download
 */
fun BucketApi.authenticatedRequest(path: String): Pair<String?, String> {
    val url = authenticatedUrl(path)
    val token = supabaseClient.pluginManager.getPluginOrNull(GoTrue)?.currentAccessTokenOrNull()
    return token to url
}
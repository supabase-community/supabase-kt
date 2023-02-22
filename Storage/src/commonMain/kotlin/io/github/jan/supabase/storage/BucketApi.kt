package io.github.jan.supabase.storage

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.putJsonObject
import io.github.jan.supabase.safeBody
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.defaultForFilePath
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlin.time.Duration

sealed interface BucketApi {

    val bucketId: String
    val supabaseClient: SupabaseClient

    /**
     * Uploads a file in [bucketId] under [path]
     * @param path The path to upload the file to
     * @return the key to the uploaded file
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun upload(path: String, data: ByteArray): String

    /**
     * Updates a file in [bucketId] under [path]
     * @return the key to the updated file
     * @throws RestException or one of its subclasses if receiving an error response
     * @throws HttpRequestTimeoutException if the request timed out
     * @throws HttpRequestException on network related issues
     */
    suspend fun update(path: String, data: ByteArray): String

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

    override suspend fun update(path: String, data: ByteArray): String = uploadOrUpdate(HttpMethod.Put, bucketId, path, data)

    override suspend fun upload(path: String, data: ByteArray): String = uploadOrUpdate(HttpMethod.Post, bucketId, path, data)

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
        val transformation = ImageTransformation().apply(transform).queryString()
        val url = if(transformation.isBlank()) authenticatedUrl(path) else authenticatedRenderUrl(path, transform)
        return storage.api.rawRequest(url) {
            method = HttpMethod.Get
        }.body()
    }

    override suspend fun downloadPublic(path: String, transform: ImageTransformation.() -> Unit): ByteArray {
        val transformation = ImageTransformation().apply(transform).queryString()
        val url = if(transformation.isBlank()) publicUrl(path) else publicRenderUrl(path, transform)
        return storage.api.rawRequest(url) {
            method = HttpMethod.Get
        }.body()
    }

    override suspend fun list(prefix: String, filter: BucketListFilter.() -> Unit): List<BucketItem> {
        return storage.api.postJson("object/list/$bucketId", buildJsonObject {
            put("prefix", prefix)
            putJsonObject(BucketListFilter().apply(filter).build())
        }).safeBody()
    }

    private suspend fun uploadOrUpdate(method: HttpMethod, bucket: String, path: String, body: ByteArray): String {
        return storage.api.request("object/$bucket/$path") {
            this.method = method
            setBody(body)

            header(HttpHeaders.ContentType, ContentType.defaultForFilePath(path))
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
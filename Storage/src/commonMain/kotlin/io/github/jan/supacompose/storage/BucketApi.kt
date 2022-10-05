package io.github.jan.supacompose.storage

import io.github.jan.supacompose.SupabaseClient
import io.github.jan.supacompose.auth.auth
import io.github.jan.supacompose.auth.currentAccessToken
import io.github.jan.supacompose.putJsonObject
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
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
     */
    suspend fun upload(path: String, data: ByteArray): String

    /**
     * Updates a file in [bucketId] under [path]
     * @return the key to the updated file
     */
    suspend fun update(path: String, data: ByteArray): String

    /**
     * Deletes all files in [bucketId] with in [paths]
     */
    suspend fun delete(paths: Collection<String>)

    /**
     * Deletes all files in [bucketId] with in [paths]
     */
    suspend fun delete(vararg paths: String) = delete(paths.toList())

    /**
     * Moves a file under [from] to [to]
     */
    suspend fun move(from: String, to: String)

    /**
     * Copies a file under [from] to [to]
     */
    suspend fun copy(from: String, to: String)

    /**
     * Creates a signed url to download without authentication. The url will expire after [expiresIn]
     * @param path The path to create an url for
     * @param expiresIn The duration the url is valid
     * @return The url to download the file
     */
    suspend fun createSignedUrl(path: String, expiresIn: Duration): String

    /**
     * Creates signed urls for all specified paths. The urls will expire after [expiresIn]
     * @param expiresIn The duration the urls are valid
     * @param paths The paths to create urls for
     * @return A list of [SignedUrl]s
     */
    suspend fun createSignedUrls(expiresIn: Duration, paths: Collection<String>): List<SignedUrl>

    /**
     * Creates signed urls for all specified paths. The urls will expire after [expiresIn]
     * @param expiresIn The duration the urls are valid
     * @param paths The paths to create urls for
     * @return A list of [SignedUrl]s
     */
    suspend fun createSignedUrls(expiresIn: Duration, vararg paths: String) = createSignedUrls(expiresIn, paths.toList())

    /**
     * Downloads a file from [bucketId] under [path]
     * @param path The path to download
     * @return The file as a byte array
     */
    suspend fun downloadAuthenticated(path: String): ByteArray

    /**
     * Downloads a file from [bucketId] under [path] using the public url
     * @param path The path to download
     * @return The file as a byte array
     */
    suspend fun downloadPublic(path: String): ByteArray

    /**
     * Searches for buckets with the given [prefix] and [filter]
     * @return The filtered buckets
     */
    suspend fun list(prefix: String, filter: BucketListFilter.() -> Unit = {}): List<BucketItem>

    /**
     * Changes the bucket's public status to [public]
     */
    suspend fun changePublicStatusTo(public: Boolean)

    /**
     * Returns the public url of [path]
     */
    fun publicUrl(path: String): String

    /**
     * Returns the authenticated url of [path]. Requires bearer token authentication using the user's access token
     */
    fun authenticatedUrl(path: String): String
    
}

internal class BucketApiImpl(override val bucketId: String, val storage: StorageImpl) : BucketApi {

    override val supabaseClient = storage.supabaseClient

    override suspend fun update(path: String, data: ByteArray): String = uploadOrUpdate(HttpMethod.Put, bucketId, path, data)

    override suspend fun upload(path: String, data: ByteArray): String = uploadOrUpdate(HttpMethod.Post, bucketId, path, data)

    override suspend fun delete(paths: Collection<String>) {
        storage.makeRequest(HttpMethod.Delete, "object/$bucketId") {
            setBody(buildJsonObject {
                putJsonArray("prefixes") {
                    paths.forEach(this::add)
                }
            })
        }
    }

    override suspend fun move(from: String, to: String) {
        storage.makeRequest(HttpMethod.Post, "object/move") {
            setBody(buildJsonObject {
                put("bucketId", bucketId)
                put("sourceKey", from)
                put("destinationKey", to)
            })
        }
    }

    override suspend fun copy(from: String, to: String) {
        storage.makeRequest(HttpMethod.Post, "object/copy") {
            setBody(buildJsonObject {
                put("bucketId", bucketId)
                put("sourceKey", from)
                put("destinationKey", to)
            })
        }
    }

    override suspend fun createSignedUrl(path: String, expiresIn: Duration): String {
        return storage.resolveUrl(
            storage.makeRequest(HttpMethod.Post, "object/sign/$bucketId/$path") {
                setBody(buildJsonObject {
                    put("expiresIn", expiresIn.inWholeSeconds)
                })
            }.body<JsonObject>()["signedURL"]?.jsonPrimitive?.content?.substring(1)
                ?: throw IllegalStateException("Expected signed url in response")
        )
    }

    override suspend fun createSignedUrls(expiresIn: Duration, paths: Collection<String>): List<SignedUrl> {
        return storage.makeRequest(HttpMethod.Post, "object/sign/$bucketId") {
            setBody(buildJsonObject {
                putJsonArray("paths") {
                    paths.forEach(this::add)
                }
                put("expiresIn", expiresIn.inWholeSeconds)
            })
        }.body<List<SignedUrl>>().map {
            it.copy(signedURL = storage.resolveUrl(it.signedURL.substring(1)))
        }
    }

    override suspend fun downloadAuthenticated(path: String): ByteArray {
        return storage.makeRequest(HttpMethod.Get, "object/authenticated/$bucketId/$path").body()
    }

    override suspend fun downloadPublic(path: String): ByteArray {
        return storage.makeRequest(HttpMethod.Get, publicUrl(path)).body()
    }

    override suspend fun list(prefix: String, filter: BucketListFilter.() -> Unit): List<BucketItem> {
        return storage.makeRequest(HttpMethod.Post, "object/list/$bucketId") {
            setBody(buildJsonObject {
                put("prefix", prefix)
                putJsonObject(BucketListFilter().apply(filter).build())
            })
        }.body<List<BucketItem>>()
    }

    private suspend fun uploadOrUpdate(method: HttpMethod, bucket: String, path: String, body: ByteArray): String {
        return storage.makeRequest(method, "object/$bucket/$path", false) {
            setBody(body)
        }.body<JsonObject>()["Key"]?.jsonPrimitive?.content ?: throw IllegalStateException("Expected a key in a upload response")
    }

    override suspend fun changePublicStatusTo(public: Boolean) = storage.changePublicStatus(bucketId, public)

    override fun authenticatedUrl(path: String) = storage.resolveUrl("object/authenticated/$bucketId/$path")

    override fun publicUrl(path: String) = storage.resolveUrl("object/public/$bucketId/$path")

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
    val token = supabaseClient.auth.currentAccessToken()
    return token to url
}
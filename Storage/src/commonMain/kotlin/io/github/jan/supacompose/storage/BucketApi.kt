package io.github.jan.supacompose.storage

import io.github.jan.supacompose.putJsonObject
import io.ktor.client.call.body
import io.ktor.client.request.setBody
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

    /**
     * Uploads a file in [bucketId] under [path]
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
     * @return A map of paths to urls
     */
    suspend fun createSignedUrls(expiresIn: Duration, paths: Collection<String>): List<SignedUrl>

    /**
     * Creates signed urls for all specified paths. The urls will expire after [expiresIn]
     * @param expiresIn The duration the urls are valid
     * @param paths The paths to create urls for
     * @return A map of paths to urls
     */
    suspend fun createSignedUrls(expiresIn: Duration, vararg paths: String) = createSignedUrls(expiresIn, paths.toList())

    /**
     * Downloads a file from [bucketId] under [path]
     * @param path The path to download
     * @return The file as a byte array
     */
    suspend fun download(path: String): ByteArray

    suspend fun list(prefix: String, filter: BucketListFilter.() -> Unit = {}): List<BucketItem>

}

internal class BucketApiImpl(override val bucketId: String, val storage: StorageImpl) : BucketApi {

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
        return storage.path(
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
            it.copy(signedURL = storage.path(it.signedURL.substring(1)))
        }
    }

    override suspend fun download(path: String): ByteArray {
        return storage.makeRequest(HttpMethod.Get, "object/authenticated/$bucketId/$path").body()
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
        return storage.makeRequest(method, "object/$bucket/$path") {
            setBody(body)
        }.body<JsonObject>()["Key"]?.jsonPrimitive?.content ?: throw IllegalStateException("Expected a key in a upload response")
    }

}
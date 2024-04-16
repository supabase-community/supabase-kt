import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.testing.createMockedSupabaseClient
import io.github.jan.supabase.testing.pathAfterVersion
import io.github.jan.supabase.testing.toJsonElement
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class StorageTest {

    private val configureClient: SupabaseClientBuilder.() -> Unit = {
        install(Storage)
    }

    @Test
    fun testCreateBucket() {
        runTest {
            val name = "test-bucket"
            val public = true
            val fileSizeLimit = "20mb"
            val allowedMimeType = listOf("image/jpeg", "image/png")
            val client = createMockedSupabaseClient(configuration = configureClient) {
                val body = it.body.toJsonElement().jsonObject
                assertEquals(HttpMethod.Post, it.method, "Method should be POST")
                assertEquals("/bucket", it.url.pathAfterVersion(), "URL should end with 'bucket'")
                assertEquals(body["name"]?.jsonPrimitive?.content, name, "Name should be $name")
                assertEquals(body["public"]?.jsonPrimitive?.boolean, public, "Public should be $public")
                assertEquals(body["file_size_limit"]?.jsonPrimitive?.content, fileSizeLimit, "File size limit should be $fileSizeLimit")
                assertEquals(body["allowed_mime_types"]?.jsonArray?.toStringArray(), allowedMimeType, "Allowed mime type should be $allowedMimeType")
                respond("")
            }
            client.storage.createBucket(name) {
                this.public = public
                this.fileSizeLimit = 20.megabytes
                allowedMimeTypes(*allowedMimeType.toTypedArray())
            }
        }
    }

    @Test
    fun testUpdateBucket() {
        runTest {
            val name = "test-bucket"
            val public = false
            val fileSizeLimit = "10mb"
            val allowedMimeType = listOf("image/jpeg")
            val client = createMockedSupabaseClient(configuration = configureClient) {
                val body = it.body.toJsonElement().jsonObject
                assertEquals("/bucket/$name", it.url.pathAfterVersion(), "URL should end with 'bucket/$name'")
                assertEquals(HttpMethod.Put, it.method, "Method should be PUT")
                assertEquals(body["public"]?.jsonPrimitive?.boolean, public, "Public should be $public")
                assertEquals(body["file_size_limit"]?.jsonPrimitive?.content, fileSizeLimit, "File size limit should be $fileSizeLimit")
                assertEquals(body["allowed_mime_types"]?.jsonArray?.toStringArray(), allowedMimeType, "Allowed mime type should be $allowedMimeType")
                respond("")
            }
            client.storage.updateBucket(name) {
                this.public = public
                this.fileSizeLimit = 10.megabytes
                allowedMimeTypes(*allowedMimeType.toTypedArray())
            }
        }
    }

    @Test
    fun testDeleteBucket() {
        runTest {
            val name = "test-bucket"
            val client = createMockedSupabaseClient(configuration = configureClient) {
                assertEquals("/bucket/$name", it.url.pathAfterVersion(), "URL should end with '$name'")
                assertEquals(HttpMethod.Delete, it.method, "Method should be DELETE")
                respond("")
            }
            client.storage.deleteBucket(name)
        }
    }

    @Test
    fun testEmptyBucket() {
        runTest {
            val name = "test-bucket"
            val client = createMockedSupabaseClient(configuration = configureClient) {
                assertEquals("/bucket/$name/empty", it.url.pathAfterVersion(), "URL should end with '$name/empty'")
                assertEquals(HttpMethod.Post, it.method, "Method should be POST")
                respond("")
            }
            client.storage.emptyBucket(name)
        }
    }

    @Test
    fun testListBuckets() {
        runTest {
            val expectedId = "test-bucket"
            val expectedPublic = true
            val expectedFileSizeLimit = 10000
            val expectedAllowedMimeTypes = listOf("image/jpeg", "image/png")
            val expectedCreatedAt = Clock.System.now()
            val expectedUpdatedAt = Clock.System.now()
            val owner = "uuid"
            val client = createMockedSupabaseClient(configuration = configureClient) {
                assertEquals("/bucket", it.url.pathAfterVersion(), "URL should end with 'bucket'")
                assertEquals(HttpMethod.Get, it.method, "Method should be GET")
                respond(
                    """
                    [
                        {
                            "id": "$expectedId",
                            "name": "$expectedId",
                            "public": $expectedPublic,
                            "file_size_limit": $expectedFileSizeLimit,
                            "allowed_mime_types": ${expectedAllowedMimeTypes.joinToString(prefix = "[", postfix = "]", transform = { s -> "\"$s\"" })},
                            "created_at": "$expectedCreatedAt",
                            "updated_at": "$expectedUpdatedAt"
                            "owner": "$owner"
                        }
                    ]
                    """
                )
            }
            val buckets = client.storage.retrieveBuckets()
            assertEquals(1, buckets.size, "Buckets should contain 1 item")
            assertEquals("test-bucket", buckets[0].id, "Bucket id should be 'test-bucket'")
            assertEquals("test-bucket", buckets[0].name, "Bucket name should be 'test-bucket'")
            assertEquals(true, buckets[0].public, "Bucket public should be true")
            assertEquals(10000, buckets[0].fileSizeLimit, "Bucket file size limit should be 10000")
            assertEquals(expectedCreatedAt, buckets[0].createdAt, "Bucket created at should be $expectedCreatedAt")
            assertEquals(expectedUpdatedAt, buckets[0].updatedAt, "Bucket updated at should be $expectedUpdatedAt")
            assertEquals(owner, buckets[0].owner, "Bucket owner should be $owner")
            assertEquals(listOf("image/jpeg", "image/png"), buckets[0].allowedMimeTypes, "Bucket allowed mime types should be ['image/jpeg', 'image/png']")
        }
    }

    @Test
    fun testRetrieveBucket() {
        runTest {
            val expectedId = "test-bucket"
            val expectedPublic = true
            val expectedFileSizeLimit = 10000
            val expectedAllowedMimeTypes = listOf("image/jpeg", "image/png")
            val expectedCreatedAt = Clock.System.now()
            val expectedUpdatedAt = Clock.System.now()
            val owner = "uuid"
            val client = createMockedSupabaseClient(configuration = configureClient) {
                assertEquals("/bucket/$expectedId", it.url.pathAfterVersion(), "URL should end with 'bucket/$expectedId'")
                assertEquals(HttpMethod.Get, it.method, "Method should be GET")
                respond(
                    """
                    {
                        "id": "$expectedId",
                        "name": "$expectedId",
                        "public": $expectedPublic,
                        "file_size_limit": $expectedFileSizeLimit,
                        "allowed_mime_types": ${expectedAllowedMimeTypes.joinToString(prefix = "[", postfix = "]", transform = { s -> "\"$s\"" })},
                        "created_at": "$expectedCreatedAt",
                        "updated_at": "$expectedUpdatedAt"
                        "owner": "$owner"
                    }
                    """
                )
            }
            val bucket = client.storage.retrieveBucketById(expectedId)
            assertEquals("test-bucket", bucket?.id, "Bucket id should be 'test-bucket'")
            assertEquals("test-bucket", bucket?.name, "Bucket name should be 'test-bucket'")
            assertEquals(true, bucket?.public, "Bucket public should be true")
            assertEquals(10000, bucket?.fileSizeLimit, "Bucket file size limit should be 10000")
            assertEquals(expectedCreatedAt, bucket?.createdAt, "Bucket created at should be $expectedCreatedAt")
            assertEquals(expectedUpdatedAt, bucket?.updatedAt, "Bucket updated at should be $expectedUpdatedAt")
            assertEquals(owner, bucket?.owner, "Bucket owner should be $owner")
            assertEquals(listOf("image/jpeg", "image/png"), bucket?.allowedMimeTypes, "Bucket allowed mime types should be ['image/jpeg', 'image/png']")
        }
    }

    @Test
    fun testAuthHeaderWhenAuthInstalled() {
        runTest {
            val key = "test-key"
            val client = createMockedSupabaseClient(
                configuration = {
                    install(Storage)
                    install(Auth) {
                        autoLoadFromStorage = false
                        autoSaveToStorage = false
                    }
                }
            ) {
                assertEquals("Bearer $key", it.headers["Authorization"], "Authorization header should be 'Bearer test-key'")
                respond("[]")
            }
            client.auth.importAuthToken(key)
            client.storage.retrieveBuckets()
        }
    }

    private fun JsonArray.toStringArray(): List<String> {
        return map { it.jsonPrimitive.content }
    }

}
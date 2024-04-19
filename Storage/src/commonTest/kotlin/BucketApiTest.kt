import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.ImageTransformation
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.resumable.MemoryResumableCache
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.testing.assertMethodIs
import io.github.jan.supabase.testing.assertPathIs
import io.github.jan.supabase.testing.createMockedSupabaseClient
import io.github.jan.supabase.testing.pathAfterVersion
import io.github.jan.supabase.testing.toJsonElement
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

class BucketApiTest {

    private val configureClient: SupabaseClientBuilder.() -> Unit = {
        install(Storage) {
            resumable {
                cache = MemoryResumableCache()
            }
        }
    }
    private val bucketId = "bucketId"

    @Test
    fun testUpload() {
        testUploadMethod(
            method = HttpMethod.Post,
            urlPath = "/object/$bucketId/data.png",
            request = { client, expectedPath, data ->
                client.storage[bucketId].upload(expectedPath, data)
            },
            extra = {
                assertEquals(
                    "false",
                    it.headers[BucketApi.UPSERT_HEADER],
                    "Upsert header should be false"
                )
            }
        )
    }

    @Test
    fun testUploadUpsert() {
        testUploadMethod(
            method = HttpMethod.Post,
            urlPath = "/object/$bucketId/data.png",
            extra = {
                assertEquals(
                    "true",
                    it.headers[BucketApi.UPSERT_HEADER],
                    "Upsert header should be true"
                )
            },
            request = { client, expectedPath, data ->
                client.storage[bucketId].upload(expectedPath, data, upsert = true)
            }
        )
    }

    @Test
    fun testUploadToSignedUrl() {
        val expectedToken = "12345"
        testUploadMethod(
            method = HttpMethod.Put,
            urlPath = "/object/upload/sign/$bucketId/data.png",
            extra = {
                assertEquals(
                    "false",
                    it.headers[BucketApi.UPSERT_HEADER],
                    "Upsert header should be true"
                )
                assertEquals(
                    expectedToken,
                    it.url.parameters["token"],
                    "Token should be $expectedToken"
                )
            },
            request = { client, expectedPath, data ->
                client.storage[bucketId].uploadToSignedUrl(path = expectedPath, token = expectedToken, data = data, upsert = false)
            }
        )
    }

    @Test
    fun testUploadToSignedUrlUpsert() {
        val expectedToken = "12345"
        testUploadMethod(
            method = HttpMethod.Put,
            urlPath = "/object/upload/sign/$bucketId/data.png",
            extra = {
                assertEquals(
                    "true",
                    it.headers[BucketApi.UPSERT_HEADER],
                    "Upsert header should be true"
                )
                assertEquals(
                    expectedToken,
                    it.url.parameters["token"],
                    "Token should be $expectedToken"
                )
            },
            request = { client, expectedPath, data ->
                client.storage[bucketId].uploadToSignedUrl(path = expectedPath, token = expectedToken, data = data, upsert = true)
            }
        )
    }

    @Test
    fun testUpdate() {
        testUploadMethod(
            method = HttpMethod.Put,
            urlPath = "/object/$bucketId/data.png",
            request = { client, expectedPath, data ->
                client.storage[bucketId].update(expectedPath, data)
            },
            extra = {
                assertEquals(
                    "false",
                    it.headers[BucketApi.UPSERT_HEADER],
                    "Upsert header should be false"
                )
            }
        )
    }

    @Test
    fun testUpdateUpsert() {
        testUploadMethod(
            method = HttpMethod.Put,
            urlPath = "/object/$bucketId/data.png",
            extra = {
                assertEquals(
                    "true",
                    it.headers[BucketApi.UPSERT_HEADER],
                    "Upsert header should be true"
                )
            },
            request = { client, expectedPath, data ->
                client.storage[bucketId].update(expectedPath, data, upsert = true)
            }
        )
    }

    @Test
    fun testDelete() {
        runTest {
            val expectedPrefixes = listOf("data.png", "data2.png", "data3.png")
            val client = createMockedSupabaseClient(configuration = configureClient) {
                assertMethodIs(HttpMethod.Delete, it.method)
                assertPathIs("/object/$bucketId", it.url.pathAfterVersion())
                val body = it.body.toJsonElement().jsonObject["prefixes"]?.jsonArray
                assertNotNull(body, "Prefixes element should not be null")
                assertContentEquals(expectedPrefixes, body.map { e -> e.jsonPrimitive.content }, "Body should be $expectedPrefixes")
                respond("")
            }
            client.storage[bucketId].delete(expectedPrefixes)
        }
    }

    @Test
    fun testMove() {
        runTest {
            val expectedFrom = "data.png"
            val expectedTo = "data2.png"
            val expectedToBucket = "bucket2"
            val client = createMockedSupabaseClient(configuration = configureClient) {
                assertMethodIs(HttpMethod.Post, it.method)
                assertPathIs("/object/move", it.url.pathAfterVersion())
                val content = it.body.toJsonElement().jsonObject
                assertEquals(expectedFrom, content["sourceKey"]?.jsonPrimitive?.content, "From should be '$expectedFrom'")
                assertEquals(expectedTo, content["destinationKey"]?.jsonPrimitive?.content, "To should be '$expectedTo'")
                assertEquals(bucketId, content["bucketId"]?.jsonPrimitive?.content, "Bucket should be '$bucketId'")
                assertEquals(expectedToBucket, content["destinationBucket"]?.jsonPrimitive?.content, "To bucket should be '$expectedToBucket'")
                respond("")
            }
            client.storage[bucketId].move(expectedFrom, expectedTo, expectedToBucket)
        }
    }

    @Test
    fun testCopy() {
        runTest {
            val expectedFrom = "data.png"
            val expectedTo = "data2.png"
            val expectedToBucket = "bucket2"
            val client = createMockedSupabaseClient(configuration = configureClient) {
                assertMethodIs(HttpMethod.Post, it.method)
                assertPathIs("/object/copy", it.url.pathAfterVersion())
                val content = it.body.toJsonElement().jsonObject
                assertEquals(expectedFrom, content["sourceKey"]?.jsonPrimitive?.content, "From should be '$expectedFrom'")
                assertEquals(expectedTo, content["destinationKey"]?.jsonPrimitive?.content, "To should be '$expectedTo'")
                assertEquals(bucketId, content["bucketId"]?.jsonPrimitive?.content, "Bucket should be '$bucketId'")
                assertEquals(expectedToBucket, content["destinationBucket"]?.jsonPrimitive?.content, "To bucket should be '$expectedToBucket'")
                respond("")
            }
            client.storage[bucketId].copy(expectedFrom, expectedTo, expectedToBucket)
        }
    }

    @Test
    fun testCreateSignedUrl() {
        runTest {
            val expectedPath = "data.png"
            val expectedExpiresIn = 120.seconds
            val expectedHeight = 100
            val expectedWidth = 100
            val expectedQuality = 80
            val expectedResize = ImageTransformation.Resize.COVER
            val expectedFormat = "origin"
            val expectedUrl = "/object/sign/avatars/folder/cat.png?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJhdmF0YXJzL2ZvbGRlci9jYXQucG5nIiwiaWF0IjoxNjE3NzI2MjczLCJleHAiOjE2MTc3MjcyNzN9.s7Gt8ME80iREVxPhH01ZNv8oUn4XtaWsmiQ5csiUHn4"
            val client = createMockedSupabaseClient(configuration = configureClient) {
                assertMethodIs(HttpMethod.Post, it.method)
                assertPathIs("/object/sign/$bucketId/$expectedPath", it.url.pathAfterVersion())
                val content = it.body.toJsonElement().jsonObject
                val transform = content["transform"]?.jsonObject
                assertEquals(
                    expectedExpiresIn,
                    content["expiresIn"]?.jsonPrimitive?.long?.seconds,
                    "Expires in should be $expectedExpiresIn"
                )
                assertEquals(expectedWidth, transform?.get("width")?.jsonPrimitive?.int, "Width should be $expectedWidth")
                assertEquals(expectedHeight, transform?.get("height")?.jsonPrimitive?.int, "Height should be $expectedHeight")
                assertEquals(expectedQuality, transform?.get("quality")?.jsonPrimitive?.int, "Quality should be $expectedQuality")
                assertEquals(expectedResize.name.lowercase(), transform?.get("resize")?.jsonPrimitive?.content, "Resize should be ${expectedResize.name.lowercase()}")
                assertEquals(expectedFormat, transform?.get("format")?.jsonPrimitive?.content, "Format should be $expectedFormat")
                respond(
                    content = """
                    { 
                        "signedURL": "$expectedUrl"
                    }
                    """.trimIndent(),
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    )
                )
            }
            val url = client.storage[bucketId].createSignedUrl(expectedPath, expectedExpiresIn) {
                size(expectedWidth, expectedHeight)
                format = expectedFormat
                quality = expectedQuality
                resize = expectedResize
            }
            assertEquals(client.storage.resolveUrl(expectedUrl.substring(1)), url, "URL should be $expectedUrl")
        }
    }

    private fun testUploadMethod(
        method: HttpMethod,
        urlPath: String,
        expectedPath: String = "data.png",
        extra: suspend MockRequestHandleScope.(HttpRequestData) -> Unit,
        request: suspend (client: SupabaseClient, expectedPath: String, data: ByteArray) -> String
    ) {
        runTest {
            val expectedData = byteArrayOf(1, 2, 3)
            val client = createMockedSupabaseClient(configuration = configureClient) {
                val data = it.body.toByteArray()
                assertMethodIs(method, it.method)
                assertPathIs(urlPath, it.url.pathAfterVersion())
                assertContentEquals(expectedData, data, "Data should be [1, 2, 3]")
                assertEquals(ContentType.Image.PNG, it.body.contentType, "Content type should be image/png")
                extra(this, it)
                respond(
                    content = """
                    { 
                        "Key": "$expectedPath"
                    }
                    """.trimIndent(),
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    )
                )
            }
            val key = request(client, expectedPath, expectedData)
            assertEquals(expectedPath, key, "Key should be $expectedPath")
        }
    }

}
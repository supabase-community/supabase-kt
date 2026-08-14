import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.StorageRestException
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.testing.assertMethodIs
import io.github.jan.supabase.testing.assertPathIs
import io.github.jan.supabase.testing.createMockedSupabaseClient
import io.github.jan.supabase.testing.pathAfterVersion
import io.github.jan.supabase.testing.respondJson
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StorageRestExceptionTest {

    private val configureClient: SupabaseClientBuilder.() -> Unit = {
        install(Storage)
    }

    private lateinit var client: SupabaseClient

    @Test
    fun testCreateBucketThrowsStorageRestException() {
        runTest {
            client = createMockedSupabaseClient(configuration = configureClient) {
                assertPathIs("/bucket", it.url.pathAfterVersion())
                assertMethodIs(HttpMethod.Post, it.method)
                respondJson(
                    """{"statusCode":409,"error":"bucket_already_exists","message":"Bucket already exists","code":"ResourceAlreadyExists"}""",
                    HttpStatusCode.Conflict
                )
            }

            val exception = assertFailsWith<StorageRestException> {
                client.storage.createBucket("bucket")
            }

            assertEquals("bucket_already_exists", exception.error)
            assertEquals("Bucket already exists", exception.description)
            assertEquals(409, exception.statusCode)
            assertEquals("ResourceAlreadyExists", exception.code)
        }
    }

    @Test
    fun testCreateBucketFallsBackToUnknownErrorWhenBodyCannotBeParsed() {
        runTest {
            client = createMockedSupabaseClient(configuration = configureClient) {
                respondError(HttpStatusCode.InternalServerError, "maintenance")
            }

            val exception = assertFailsWith<StorageRestException> {
                client.storage.createBucket("bucket")
            }

            assertEquals("Unknown error", exception.error)
            assertEquals("", exception.description)
            assertEquals(HttpStatusCode.InternalServerError.value, exception.statusCode)
            assertEquals("", exception.code)
        }
    }

    @AfterTest
    fun cleanup() {
        runTest {
            if(::client.isInitialized) {
                client.close()
            }
        }
    }

}

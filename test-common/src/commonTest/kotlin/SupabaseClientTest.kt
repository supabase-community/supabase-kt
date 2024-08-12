import io.github.jan.supabase.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.testing.createMockedSupabaseClient
import io.ktor.client.engine.mock.respond
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class SupabaseClientTest {

    @Test
    fun testClientInfoHeader() {
        runTest {
            val client = createMockedSupabaseClient(
                supabaseUrl = "https://example.supabase.co",
                supabaseKey = "somekey",
                requestHandler = {
                    assertEquals(
                        "supabase-kt/${BuildConfig.PROJECT_VERSION}",
                        it.headers["X-Client-Info"],
                        "X-Client-Info header should be set to 'supabase-kt/${BuildConfig.PROJECT_VERSION}'"
                    )
                    respond("")
                }
            )
            client.httpClient.get("")
        }
    }

    @Test
    fun testAccessTokenProvider() {
        runTest {
            val client = createMockedSupabaseClient(
                configuration = {
                    accessToken = {
                        "myToken"
                    }
                }
            )
            assertEquals("myToken", client.accessTokenProvider?.invoke())
        }
    }

    @Test
    fun testDefaultLogLevel() {
        createMockedSupabaseClient(
            configuration = {
                defaultLogLevel = LogLevel.DEBUG
            }
        )
        assertEquals(
            LogLevel.DEBUG,
            SupabaseClient.DEFAULT_LOG_LEVEL,
            "Default log level should be set to ${LogLevel.DEBUG}"
        )
    }

    @Test
    fun testDefaultSerializer() {
        val client = createMockedSupabaseClient(
            configuration = {
                defaultSerializer = DummySerializer()
            }
        )
        assertIs<DummySerializer>(client.defaultSerializer, "Default serializer should be an instance of DummySerializer")
    }

    @Test
    fun testClientBuilderParametersWithHttpsUrl() {
        val client = createMockedSupabaseClient(
            supabaseUrl = "https://example.supabase.co",
            supabaseKey = "somekey"
        )
        assertEquals("example.supabase.co", client.supabaseUrl, "Supabase url should not contain https://")
        assertEquals("somekey", client.supabaseKey, "Supabase key should be set to somekey")
        assertEquals(
            "https://example.supabase.co",
            client.supabaseHttpUrl,
            "Supabase http url should be https://example.supabase.co"
        )
    }

    @Test
    fun testClientBuilderPlugins() {
        val client = createMockedSupabaseClient(
            supabaseUrl = "example.supabase.co",
            supabaseKey = "somekey",
            configuration =  {
                install(TestPlugin) {
                    testValue = true
                }
            }
        )
        val plugin = client.pluginManager.getPluginOrNull(TestPlugin)
        //test if the plugin was correctly installed
        assertNotNull(plugin, "Plugin 'test' should not be null")
        //test if the plugin correctly modified the 'useHTTPS' parameter
        assertEquals(
            "https://example.supabase.co",
            client.supabaseHttpUrl,
            "Supabase http url should be https://example.supabase.co because the plugin modifies it"
        )
    }

}
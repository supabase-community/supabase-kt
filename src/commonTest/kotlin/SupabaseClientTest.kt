import io.github.jan.supabase.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestPlugin(private val config: Config) : SupabasePlugin {

    data class Config(var testValue: Boolean = false)

    companion object : SupabasePluginProvider<Config, TestPlugin> {

        override val key = "test"

        override fun createConfig(init: Config.() -> Unit): Config {
            return Config().apply(init)
        }

        override fun create(supabaseClient: SupabaseClient, config: Config): TestPlugin {
            return TestPlugin(config)
        }

        override fun setup(builder: SupabaseClientBuilder, config: Config) {
            builder.useHTTPS = config.testValue
        }

    }

}

class SupabaseClientTest {

    private val mockEngine = MockEngine { data ->
        val headers = data.headers
        if("X-Client-Info" !in headers) return@MockEngine respond("Missing X-Client-Info header", HttpStatusCode.BadRequest)
        val clientInfo = headers["X-Client-Info"]!!
        if(clientInfo != "supabase-kt/${BuildConfig.PROJECT_VERSION}") return@MockEngine respond("Invalid X-Client-Info header", HttpStatusCode.BadRequest)
        respond("") //ignore for this test
    }

    @Test
    fun testClientHeader() {
        runTest {
            val client = createMockedSupabaseClient(
                supabaseUrl = "https://example.supabase.co",
                supabaseKey = "somekey"
            ) {

            }
            val response = client.httpClient.get("")
            assertEquals(HttpStatusCode.OK, response.status, "Status code should be OK")
        }
    }

    @Test
    fun testClientBuilderParametersWithHttpsUrl() {
        val client = createMockedSupabaseClient(
            supabaseUrl = "https://example.supabase.co",
            supabaseKey = "somekey"
        ) {

        }
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
            supabaseKey = "somekey"
        ) {
            install(TestPlugin) {
                testValue = true
            }
        }
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

    private fun createMockedSupabaseClient(
        supabaseUrl: String,
        supabaseKey: String,
        init: SupabaseClientBuilder.() -> Unit
    ): SupabaseClient {
        return createSupabaseClient(supabaseUrl, supabaseKey) {
            httpEngine = mockEngine
            init()
        }
    }

}
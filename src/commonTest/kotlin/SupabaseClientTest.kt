import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

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

    private val mockEngine = MockEngine { _ ->
        respond("") //ignore for this test
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
        val plugin = client.pluginManager.getPlugin<Any?>("test")
        //test if the plugin was correctly installed
        assertIs<TestPlugin>(plugin, "Plugin 'test' should be of type TestPlugin")
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
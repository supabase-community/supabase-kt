import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.logging.SupabaseLogger
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider

class TestPlugin(override val config: Config, override val supabaseClient: SupabaseClient) :
    SupabasePlugin<TestPlugin.Config> {

    data class Config(var testValue: Boolean = false)

    companion object : SupabasePluginProvider<Config, TestPlugin> {

        override val key = "test"

        override val logger: SupabaseLogger = SupabaseClient.createLogger("Test")

        override fun createConfig(init: Config.() -> Unit): Config {
            return Config().apply(init)
        }

        override fun create(supabaseClient: SupabaseClient, config: Config): TestPlugin {
            return TestPlugin(config, supabaseClient)
        }

        override fun setup(builder: SupabaseClientBuilder, config: Config) {
            builder.useHTTPS = config.testValue
        }

    }

}
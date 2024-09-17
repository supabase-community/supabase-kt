package io.github.jan.supabase.coil

import coil3.ImageLoader
import coil3.fetch.Fetcher
import coil3.request.ImageRequest
import coil3.request.Options
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.logging.SupabaseLogger
import io.github.jan.supabase.logging.d
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.github.jan.supabase.storage.StorageItem
import io.github.jan.supabase.storage.storage

/**
 * A plugin that implements [Fetcher.Factory] to support using [StorageItem] as data when creating an [ImageRequest] or using it as a model in Compose Multiplatform.
 */
interface Coil3Integration: SupabasePlugin<Coil3Integration.Config>, Fetcher.Factory<StorageItem> {

    /**
     * The configuration for the coil integration.
     */
    class Config

    companion object : SupabasePluginProvider<Config, Coil3Integration> {

        override val key = "coil3"

        override val logger: SupabaseLogger = SupabaseClient.createLogger("Supabase-Coil3Integration")

        override fun create(supabaseClient: SupabaseClient, config: Config): Coil3Integration {
            return Coil3IntegrationImpl(supabaseClient, config)
        }

        override fun createConfig(init: Config.() -> Unit): Config {
            return Config().apply(init)
        }

    }

}

internal class Coil3IntegrationImpl(
    override val supabaseClient: SupabaseClient,
    override val config: Coil3Integration.Config
) : Coil3Integration {

    override fun create(data: StorageItem, options: Options, imageLoader: ImageLoader): Fetcher {
        Coil3Integration.logger.d { "Creating Storage Fetcher" }
        return SupabaseStorageFetcher(supabaseClient.storage, data, options, imageLoader)
    }

}

/**
 * With the [Coil3Integration] plugin installed, you can use this property to access the coil fetcher factory.
 */
@SupabaseExperimental
val SupabaseClient.coil3: Coil3Integration
    get() = pluginManager.getPlugin(Coil3Integration)
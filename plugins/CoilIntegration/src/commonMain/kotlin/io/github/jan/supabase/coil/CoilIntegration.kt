package io.github.jan.supabase.coil

import coil.ImageLoader
import coil.fetch.Fetcher
import coil.request.ImageRequest
import coil.request.Options
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.github.jan.supabase.storage.StorageItem
import io.github.jan.supabase.storage.storage

/**
 * A plugin that implements [Fetcher.Factory] to support using [StorageItem] as data when creating a [ImageRequest] or using it as a model in Jetpack Compose.
 */
interface CoilIntegration: SupabasePlugin, Fetcher.Factory<StorageItem> {

    /**
     * The configuration for the coil integration.
     */
    class Config

    companion object : SupabasePluginProvider<Config, CoilIntegration> {

        override val key = "coil"

        override fun create(supabaseClient: SupabaseClient, config: Config): CoilIntegration {
            return CoilIntegrationImpl(supabaseClient)
        }

        override fun createConfig(init: Config.() -> Unit): Config {
            return Config().apply(init)
        }

    }

}

internal class CoilIntegrationImpl(private val supabaseClient: SupabaseClient) : CoilIntegration {

    override fun create(data: StorageItem, options: Options, imageLoader: ImageLoader): Fetcher {
        return SupabaseStorageFetcher(supabaseClient.storage, data, options, imageLoader)
    }

}

/**
 * With the [CoilIntegration] plugin installed, you can use this property to access the coil fetcher factory.
 */
@SupabaseExperimental
val SupabaseClient.coil: CoilIntegration
    get() = pluginManager.getPlugin(CoilIntegration)
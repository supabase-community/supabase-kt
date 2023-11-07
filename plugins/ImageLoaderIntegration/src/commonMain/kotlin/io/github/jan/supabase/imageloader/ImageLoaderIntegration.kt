package io.github.jan.supabase.imageloader

import com.seiko.imageloader.component.fetcher.Fetcher
import com.seiko.imageloader.component.keyer.Keyer
import com.seiko.imageloader.option.Options
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.github.jan.supabase.storage.StorageItem
import io.github.jan.supabase.storage.storage

/**
 * A plugin that integrates Compose-ImageLoader with supabase-kt.
 */
interface ImageLoaderIntegration: SupabasePlugin, Fetcher.Factory, Keyer {

    /**
     * The configuration for the imageloader integration.
     */
    class Config

    companion object : SupabasePluginProvider<Config, ImageLoaderIntegration> {

        override val key = "imageloader"

        override fun create(supabaseClient: SupabaseClient, config: Config): ImageLoaderIntegration {
            return ImageLoaderIntegrationImpl(supabaseClient)
        }

        override fun createConfig(init: Config.() -> Unit): Config {
            return Config().apply(init)
        }

    }

}

internal class ImageLoaderIntegrationImpl(private val supabaseClient: SupabaseClient) : ImageLoaderIntegration {

    override fun create(data: Any, options: Options): Fetcher? {
        if(data !is StorageItem) return null
        return SupabaseStorageFetcher(supabaseClient.storage, data)
    }

    override fun key(data: Any, options: Options, type: Keyer.Type): String? {
        if(data !is StorageItem) return null
        return data.bucketId + data.path
    }

}

/**
 * With the [ImageLoaderIntegration] plugin installed, you can use this property to access the coil fetcher factory.
 */
@SupabaseExperimental
val SupabaseClient.imageLoader: ImageLoaderIntegration
    get() = pluginManager.getPlugin(ImageLoaderIntegration)
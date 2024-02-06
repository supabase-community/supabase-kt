package io.github.jan.supabase.imageloader

import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.component.ComponentRegistryBuilder
import com.seiko.imageloader.component.fetcher.Fetcher
import com.seiko.imageloader.component.keyer.Keyer
import com.seiko.imageloader.model.ImageRequest
import com.seiko.imageloader.option.Options
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.logging.SupabaseLogger
import io.github.jan.supabase.plugins.SupabasePlugin
import io.github.jan.supabase.plugins.SupabasePluginConfig
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.github.jan.supabase.storage.StorageItem
import io.github.jan.supabase.storage.storage

/**
 * A plugin that implements [Fetcher.Factory] and [Keyer] for [ImageLoader] to support using [StorageItem] as data when creating a [ImageRequest].
 * Use [ComponentRegistryBuilder.add] to add this component to your [ImageLoader] instance:
 * ```kotlin
 * add(keyer = supabaseClient.imageLoader)
 * add(fetcherFactory = supabaseClient.imageLoader)
 * ```
 */
interface ImageLoaderIntegration: SupabasePlugin<ImageLoaderIntegration.Config>, Fetcher.Factory, Keyer {

    /**
     * The configuration for the [ImageLoader] integration.
     */
    class Config: SupabasePluginConfig()

    companion object : SupabasePluginProvider<Config, ImageLoaderIntegration> {

        override val key = "imageloader"

        override fun create(supabaseClient: SupabaseClient, config: Config): ImageLoaderIntegration {
            return ImageLoaderIntegrationImpl(supabaseClient, config)
        }

        override fun createConfig(init: Config.() -> Unit): Config {
            return Config().apply(init)
        }

    }

}

internal class ImageLoaderIntegrationImpl(
    override val supabaseClient: SupabaseClient,
    override val config: ImageLoaderIntegration.Config
) : ImageLoaderIntegration {

    override val logger: SupabaseLogger = config.logger(config.logLevel ?: supabaseClient.logLevel, "ImageLoader Integration")

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
 * With the [ImageLoaderIntegration] plugin installed, you can use this property to access the [ImageLoader] implementations of [Fetcher.Factory] & [Keyer].
 */
@SupabaseExperimental
val SupabaseClient.imageLoader: ImageLoaderIntegration
    get() = pluginManager.getPlugin(ImageLoaderIntegration)